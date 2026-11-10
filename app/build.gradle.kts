import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// -----------------------------------------------------------------------------
// Everything that identifies the app is read from config/build.properties,
// so there is no need to edit this file for a new version.
// -----------------------------------------------------------------------------
val buildConfigFile = rootProject.file("config/build.properties")
val buildProps = Properties().apply { buildConfigFile.inputStream().use { load(it) } }
fun prop(key: String): String =
    buildProps.getProperty(key)?.trim() ?: error("Missing '$key' in config/build.properties")

// -----------------------------------------------------------------------------
// Release signing key. Created once in keystore/ (git-ignored) and reused for
// every later build, so new versions can always update the installed app.
// -----------------------------------------------------------------------------
val releaseKeystore = rootProject.file(prop("signing.storeFile"))

fun ensureReleaseKeystore() {
    if (releaseKeystore.exists()) return
    releaseKeystore.parentFile.mkdirs()
    val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    val keytool = File(System.getProperty("java.home"), if (isWindows) "bin/keytool.exe" else "bin/keytool").absolutePath
    val command = listOf(
        keytool, "-genkeypair", "-noprompt",
        "-keystore", releaseKeystore.absolutePath,
        "-storetype", "PKCS12",
        "-storepass", prop("signing.storePassword"),
        "-keypass", prop("signing.keyPassword"),
        "-alias", prop("signing.keyAlias"),
        "-keyalg", "RSA", "-keysize", "2048", "-validity", "36500",
        "-dname", "CN=${prop("app.name")}, O=vislupus",
    )
    val exit = ProcessBuilder(command).inheritIO().start().waitFor()
    check(exit == 0 && releaseKeystore.exists()) { "Could not create the release keystore" }
    logger.lifecycle("")
    logger.lifecycle("  NEW RELEASE KEY CREATED: $releaseKeystore")
    logger.lifecycle("  Back this file up. Without it, future versions cannot update the installed app.")
    logger.lifecycle("")
}

// Only touch the keystore when a release build was actually requested.
if (gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }) {
    ensureReleaseKeystore()
}

/** true when Gradle was started with -Pscreenshots. */
val screenshots = project.hasProperty("screenshots")

android {
    // Code package. Stays fixed even if app.id changes.
    namespace = "com.vislupus.mindmath"
    compileSdk = prop("sdk.compile").toInt()

    defaultConfig {
        applicationId = prop("app.id")
        minSdk = prop("sdk.min").toInt()
        targetSdk = prop("sdk.target").toInt()
        versionCode = prop("app.versionCode").toInt()
        versionName = prop("app.versionName")
        resValue("string", "app_name", prop("app.name"))
    }

    signingConfigs {
        create("release") {
            storeFile = releaseKeystore
            storePassword = prop("signing.storePassword")
            keyAlias = prop("signing.keyAlias")
            keyPassword = prop("signing.keyPassword")
        }
    }

    buildTypes {
        debug {
            // The development build installs next to the real app (own id, own
            // data), so testing never touches your real progress.
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", prop("app.name") + " Dev")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    // The top-level config/ folder is compiled as part of the app, so every
    // setting, colour and text can be edited without opening the source tree.
    sourceSets.getByName("main").kotlin.srcDir(rootProject.file("config"))

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "DebugProbesKt.bin")
    }

    // Optional screenshot tests (see README): only active with -Pscreenshots,
    // so normal builds never download or compile anything extra.
    if (screenshots) {
        sourceSets.getByName("test").kotlin.srcDir("src/screenshots/java")
        testOptions.unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("roborazzi.test.record", "true")
                it.forkEvery = 1 // a fresh process (and app) for every test class
            }
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")

    if (screenshots) {
        testImplementation(composeBom)
        testImplementation("androidx.compose.ui:ui-test-junit4")
        debugImplementation("androidx.compose.ui:ui-test-manifest")
        testImplementation("org.robolectric:robolectric:4.14.1")
        testImplementation("io.github.takahirom.roborazzi:roborazzi:1.43.0")
        testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.43.0")
    }
}
