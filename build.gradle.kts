// Root build file. Tool versions are pinned on purpose (see CLAUDE.md):
// do not upgrade Gradle, AGP or Kotlin unless compatibility has been verified.
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false
}
