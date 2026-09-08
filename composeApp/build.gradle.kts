import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

val hostOs = System.getProperty("os.name").orEmpty()
val isMacOs = hostOs.startsWith("Mac")
val isWindows = hostOs.startsWith("Windows")
val hostArch = System.getProperty("os.arch")
val hostOsFamily = hostOs.split(' ').first()
val cmakeBuildDir = layout.projectDirectory.dir("cmake/$hostArch/$hostOsFamily")
val jniDir = layout.projectDirectory.dir("libs/jni")
val staticLibDir = layout.projectDirectory.dir("libs/static")
val nativeGgwaveDir = layout.projectDirectory.dir("native/ggwave")

kotlin {
    jvmToolchain(21)

    jvm("desktop")

    android {
        namespace = "com.example.ggwavekmp.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        androidResources {
            enable = true
        }
    }

    if (isMacOs) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { iosTarget ->
            iosTarget.compilations.getByName("main").cinterops.create("nativeLibrary") {
                definitionFile.set(file("ggwave.def"))
                includeDirs("native/ggwave")
            }

            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.kotlinx.coroutines.android)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.constraintlayout.compose.multiplatform)
            implementation(libs.material.kolor)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += listOf("-Djava.library.path=libs/jni")

        nativeDistributions {
            outputBaseDir.set(layout.buildDirectory.dir("testDistribution"))

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ggwaveKMP"
            packageVersion = "1.0.0"
            description = "Compose Example App"
            copyright = "© 2024 Wooram Yang. All rights reserved."
            vendor = "Wooram Yang"
            modules("java.base", "java.desktop")
        }
    }
}

val copyJniIntoDistribution by tasks.registering(Copy::class) {
    from(jniDir)
    include("*.dll", "*.dylib", "*.so")
    into(layout.buildDirectory.dir("testDistribution/main/app/ggwaveKMP/libs/jni"))
}

tasks.matching {
    it.name in setOf(
        "packageDmg",
        "packageMsi",
        "packageDeb",
        "packageDistributionForCurrentOS",
        "packageUberJarForCurrentOS",
    )
}.configureEach {
    dependsOn(copyJniIntoDistribution)
}

if (isWindows) {
    val configureGgwaveCmake by tasks.registering(Exec::class) {
        workingDir = file("src/desktopMain")
        commandLine(
            "cmake", "-G", "Ninja",
            "-DCMAKE_BUILD_TYPE=Release",
            "-DCMAKE_C_COMPILER=gcc",
            "-DCMAKE_CXX_COMPILER=g++",
            "-DCMAKE_C_COMPILER_TARGET=x86_64-window-gnu",
            "-DCMAKE_CXX_COMPILER_TARGET=x86_64-window-gnu",
            "-B", cmakeBuildDir.asFile.absolutePath,
            "-S", ".",
        )
    }

    val buildGgwaveCmake by tasks.registering(Exec::class) {
        dependsOn(configureGgwaveCmake)
        workingDir = cmakeBuildDir.asFile
        commandLine("cmake", "--build", ".")
    }

    val moveGGwaveLibraryForWindows by tasks.registering(Copy::class) {
        dependsOn(buildGgwaveCmake)
        from(cmakeBuildDir.file("libggwave.dll"))
        into(jniDir)
    }

    tasks.register("buildGGWaveLibrary") {
        dependsOn(moveGGwaveLibraryForWindows)
    }
} else if (isMacOs) {
    val cmake = "/opt/homebrew/bin/cmake"

    val configureGgwaveCmake by tasks.registering(Exec::class) {
        workingDir = file("src/desktopMain")
        commandLine(
            cmake, "-G", "Ninja",
            "-DCMAKE_BUILD_TYPE=Release",
            "-DCMAKE_C_COMPILER=clang",
            "-DCMAKE_CXX_COMPILER=clang++",
            "-DCMAKE_APPLE_SILICON_PROCESSOR=arm64",
            "-B", cmakeBuildDir.asFile.absolutePath,
            "-S", ".",
        )
    }

    val buildGgwaveCmake by tasks.registering(Exec::class) {
        dependsOn(configureGgwaveCmake)
        workingDir = cmakeBuildDir.asFile
        commandLine(cmake, "--build", ".")
    }

    val copyGgwaveDylib by tasks.registering(Copy::class) {
        dependsOn(buildGgwaveCmake)
        from(cmakeBuildDir.file("libggwave.dylib"))
        into(jniDir)
    }

    val compileGgwaveResampler by tasks.registering(Exec::class) {
        dependsOn(copyGgwaveDylib)
        commandLine(
            "xcrun", "--sdk", "iphonesimulator", "clang++",
            "-std=c++11", "-stdlib=libc++", "-c",
            nativeGgwaveDir.file("resampler.cpp").asFile.path,
            "-o", nativeGgwaveDir.file("resampler.o").asFile.path,
        )
    }

    val compileGgwaveCpp by tasks.registering(Exec::class) {
        dependsOn(compileGgwaveResampler)
        commandLine(
            "xcrun", "--sdk", "iphonesimulator", "clang++",
            "-std=c++11", "-stdlib=libc++", "-c",
            nativeGgwaveDir.file("ggwave.cpp").asFile.path,
            "-o", nativeGgwaveDir.file("ggwave.o").asFile.path,
        )
    }

    val archiveGgwaveIos by tasks.registering(Exec::class) {
        dependsOn(compileGgwaveCpp)
        doFirst {
            staticLibDir.asFile.mkdirs()
        }
        commandLine(
            "/usr/bin/libtool", "-static", "-o",
            staticLibDir.file("libggwave.a").asFile.path,
            nativeGgwaveDir.file("ggwave.o").asFile.path,
            nativeGgwaveDir.file("resampler.o").asFile.path,
        )
    }

    val cleanGgwaveIosObjects by tasks.registering(Delete::class) {
        dependsOn(archiveGgwaveIos)
        delete(
            nativeGgwaveDir.file("ggwave.o"),
            nativeGgwaveDir.file("resampler.o"),
        )
    }

    tasks.register("buildGGWaveLibrary") {
        dependsOn(cleanGgwaveIosObjects)
    }
}

tasks.register("createGGWaveLibrary") {
    if (isWindows || isMacOs) {
        dependsOn("buildGGWaveLibrary")
    }
}

if (isMacOs) {
    tasks.withType<CInteropProcess>().configureEach {
        dependsOn("createGGWaveLibrary")
    }
}
