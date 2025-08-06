plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.example.realsensecapture.rsnative"
    compileSdk = 34

    defaultConfig {
        minSdk = 29
        externalNativeBuild {
            cmake {}
        }
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }

    sourceSets["main"].jniLibs.srcDirs("src/main/jniLibs")
}

dependencies {}
