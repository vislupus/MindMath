# MindMath - project rules

MindMath is a mental math trainer for Android (Kotlin + Jetpack Compose).
Follow these rules for every future change.

## 1. Verified toolchain (do not change without need)

| Item | Value |
| --- | --- |
| Developer machine | Windows 11 x64 |
| Java that runs Gradle | JDK 21 (Android Studio JBR), `C:\Program Files\Android\Android Studio\jbr` |
| Gradle Wrapper | 8.11.1 |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.1.20 (Compose compiler plugin 2.1.20) |
| Compose BOM | 2024.12.01 |
| Java source/target, Kotlin JVM target | 17 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 26 |
| applicationId | `com.vislupus.mindmath` |

- Java 25 fails with this configuration; Java 21 works.
- Do not upgrade Gradle, AGP, Kotlin, Compose or Java automatically. Only
  upgrade when it is necessary and compatibility has been verified by a build.
- Keep compatibility with Android 8.0 (API 26) and newer.

## 2. Building (Windows CMD or PowerShell, never Bash scripts)

```cmd
run.bat                      :: build + install + start the dev app (MindMath Dev)
build-release.bat            :: build the signed release APK
build-release.bat install    :: build the release APK and install it on the phone
```

Manual equivalent:

```cmd
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
gradlew.bat assembleRelease --no-configuration-cache
```

The APK is written to `app\build\outputs\apk\release\app-release.apk`.

Checks:

```cmd
gradlew.bat testDebugUnitTest --no-configuration-cache
gradlew.bat testDebugUnitTest -Pscreenshots --tests "*Screenshots" --no-configuration-cache
```

The first runs the maths tests. The second renders every screen to
`app\build\screenshots` (Robolectric + Roborazzi, only enabled by
`-Pscreenshots`). Use it to look at UI changes when no device is available.

## 3. Rules for every new version

- Never change `app.id` in `config/build.properties`; otherwise new versions
  cannot update the installed app.
- Increase `app.versionCode` by 1 and update `app.versionName` in
  `config/build.properties` for every release.
- Never change APK signing without an explicit warning. The release key is
  `keystore/mindmath-release.jks` (created on the first release build,
  git-ignored). A different key means the update cannot be installed.
- Keep the user's data across updates: the SQLite database, settings and
  history. When the schema changes, raise `Database.VERSION` and add a
  migration in `Database.onUpgrade`; never drop or recreate tables.
  Do not rename `AppConfig.DATABASE_DIR` / `DATABASE_NAME` or method ids
  (they are stored in the database).
- After code changes check for compile errors, missing dependencies and APIs
  that are not available on API 26.
- Never claim that a build succeeded unless it was actually run.

## 4. Project conventions

- Everything adjustable lives in the top-level `config/` folder (compiled as
  part of the app): `build.properties`, `AppConfig.kt`, `ThemeConfig.kt`,
  `IconConfig.kt`, `TextsEn.kt`, `TextsBg.kt`. New behaviour switches, colours,
  sizes and texts go there, not into the code.
- No hard-coded user-visible text in the UI. Add a field to `texts/Texts.kt`
  and a value in both `TextsEn.kt` and `TextsBg.kt`.
- All app texts default to English; Bulgarian is selectable in Settings.
  Code comments are in English.
- Icons come from Lucide only: `python tools/add_icon.py <name>`, then
  register the icon in `config/IconConfig.kt`.
- Font: bundled Roboto (`res/font`, subset with Latin + Cyrillic).
- Portrait only. Target device: Xiaomi Redmi Note 8 Pro (1080 x 2340, ~393 dp wide).
- The app has no permissions and stores everything in its private sandbox:
  `files/database/mindmath.db`.
- Use the shared components in `ui/components` so the interface stays uniform.
- A new method = one `MathMethod` object in `core/methods`, registered in
  `core/Methods.kt`, listed in `AppConfig.ENABLED_METHODS`, with texts in both
  language files. `MethodsTest` verifies answers, every solution step and the
  presence of all texts.

## 5. What to report after every update

- What was added or fixed.
- The new `versionCode` and `versionName`.
- Whether the APK was actually built, or which checks remain.
- The exact Windows commands to build it.
