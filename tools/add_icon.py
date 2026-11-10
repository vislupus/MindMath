#!/usr/bin/env python3
"""Convert Lucide icons (https://lucide.dev/icons/) to Android vector drawables.

Usage (from the project root):
    python tools/add_icon.py flame trophy chart-column

Each icon is downloaded from the official Lucide repository and written to
app/src/main/res/drawable/lucide_<name>.xml. Afterwards register it in
config/IconConfig.kt, e.g.  val flame = R.drawable.lucide_flame

Options:
    --stroke 2      stroke width in a 24x24 viewport (Lucide default is 2)
    --from DIR      read <name>.svg from a local folder instead of downloading
"""
import argparse
import pathlib
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET

RAW_URL = "https://raw.githubusercontent.com/lucide-icons/lucide/main/icons/{name}.svg"
OUT_DIR = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/res/drawable"


def num(value, default=0.0):
    return float(value) if value is not None else default


def fmt(value):
    text = f"{value:.3f}".rstrip("0").rstrip(".")
    return text if text not in ("", "-0") else "0"


def circle_path(cx, cy, rx, ry):
    return (f"M{fmt(cx - rx)},{fmt(cy)}"
            f"a{fmt(rx)},{fmt(ry)} 0 1,0 {fmt(2 * rx)},0"
            f"a{fmt(rx)},{fmt(ry)} 0 1,0 {fmt(-2 * rx)},0Z")


def rect_path(x, y, w, h, rx, ry):
    rx, ry = min(rx, w / 2), min(ry, h / 2)
    if rx == 0 or ry == 0:
        return f"M{fmt(x)},{fmt(y)}h{fmt(w)}v{fmt(h)}h{fmt(-w)}Z"
    return (f"M{fmt(x + rx)},{fmt(y)}h{fmt(w - 2 * rx)}"
            f"a{fmt(rx)},{fmt(ry)} 0 0,1 {fmt(rx)},{fmt(ry)}v{fmt(h - 2 * ry)}"
            f"a{fmt(rx)},{fmt(ry)} 0 0,1 {fmt(-rx)},{fmt(ry)}h{fmt(-(w - 2 * rx))}"
            f"a{fmt(rx)},{fmt(ry)} 0 0,1 {fmt(-rx)},{fmt(-ry)}v{fmt(-(h - 2 * ry))}"
            f"a{fmt(rx)},{fmt(ry)} 0 0,1 {fmt(rx)},{fmt(-ry)}Z")


def points_path(points, close):
    values = points.replace(",", " ").split()
    pairs = [f"{values[i]},{values[i + 1]}" for i in range(0, len(values) - 1, 2)]
    return "M" + "L".join(pairs) + ("Z" if close else "")


ARG_COUNT = {"m": 2, "l": 2, "h": 1, "v": 1, "c": 6, "s": 4, "q": 4, "t": 2, "a": 7, "z": 0}
NUMBER = re.compile(r"[+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?")


def normalize_path(d):
    """Re-write path data with explicit separators.

    Minified SVGs glue arc flags together ("a4 4 0 012.5 5.7"), which the
    Android path parser cannot read, so every number is emitted separately.
    """
    out, pos, command = [], 0, None
    while pos < len(d):
        ch = d[pos]
        if ch.isspace() or ch == ",":
            pos += 1
            continue
        if ch.isalpha():
            command = ch
            pos += 1
            if ARG_COUNT[command.lower()] == 0:
                out.append(command)
                continue
        elif command is None:
            raise ValueError("Path data must start with a command")
        count = ARG_COUNT[command.lower()]
        args = []
        for index in range(count):
            while pos < len(d) and (d[pos].isspace() or d[pos] == ","):
                pos += 1
            if command.lower() == "a" and index in (3, 4):
                args.append(d[pos])  # arc flags are always a single 0 or 1
                pos += 1
                continue
            match = NUMBER.match(d, pos)
            if not match:
                raise ValueError(f"Bad path data near position {pos}")
            number = match.group(0)
            if number.lstrip("+-").startswith("."):  # ".5" -> "0.5"
                sign = "-" if number.startswith("-") else ""
                number = sign + "0" + number.lstrip("+-")
            args.append(number)
            pos = match.end()
        out.append(command + " ".join(args))
        # Extra coordinate groups repeat the command (M continues as L).
        if command == "M":
            command = "L"
        elif command == "m":
            command = "l"
    return " ".join(out)


def element_to_path(el):
    tag = el.tag.split("}")[-1]
    a = el.attrib
    if tag == "path":
        return normalize_path(a["d"])
    if tag == "circle":
        r = num(a.get("r"))
        return circle_path(num(a.get("cx")), num(a.get("cy")), r, r)
    if tag == "ellipse":
        return circle_path(num(a.get("cx")), num(a.get("cy")), num(a.get("rx")), num(a.get("ry")))
    if tag == "rect":
        rx = a.get("rx", a.get("ry"))
        ry = a.get("ry", a.get("rx"))
        return rect_path(num(a.get("x")), num(a.get("y")), num(a.get("width")), num(a.get("height")),
                         num(rx), num(ry))
    if tag == "line":
        return f"M{a['x1']},{a['y1']}L{a['x2']},{a['y2']}"
    if tag == "polyline":
        return points_path(a["points"], close=False)
    if tag == "polygon":
        return points_path(a["points"], close=True)
    raise ValueError(f"Unsupported SVG element <{tag}>")


def convert(svg_text, stroke):
    root = ET.fromstring(svg_text)
    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        "<!-- Lucide icon (ISC License) - https://lucide.dev -->",
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '    android:width="24dp"',
        '    android:height="24dp"',
        '    android:viewportWidth="24"',
        '    android:viewportHeight="24">',
    ]
    for el in root.iter():
        if el is root:
            continue
        lines += [
            "    <path",
            f'        android:pathData="{element_to_path(el)}"',
            '        android:fillColor="#00000000"',
            '        android:strokeColor="#FF000000"',
            f'        android:strokeWidth="{fmt(stroke)}"',
            '        android:strokeLineCap="round"',
            '        android:strokeLineJoin="round" />',
        ]
    lines.append("</vector>")
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("names", nargs="+", help="Lucide icon names, e.g. flame chart-column")
    parser.add_argument("--stroke", type=float, default=2.0)
    parser.add_argument("--from", dest="source", help="local folder with Lucide .svg files")
    args = parser.parse_args()

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    failed = False
    for name in args.names:
        try:
            if args.source:
                svg = (pathlib.Path(args.source) / f"{name}.svg").read_text(encoding="utf-8")
            else:
                with urllib.request.urlopen(RAW_URL.format(name=name), timeout=30) as response:
                    svg = response.read().decode("utf-8")
            target = OUT_DIR / f"lucide_{name.replace('-', '_')}.xml"
            target.write_text(convert(svg, args.stroke), encoding="utf-8")
            print(f"ok   {name} -> {target.name}")
        except Exception as error:  # keep going so one bad name does not block the rest
            failed = True
            print(f"FAIL {name}: {error}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
