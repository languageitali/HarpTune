plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    /**
     * Namespace: Identificador para clases R y generación de código interno.
     * Se mantiene com.rosso.harptune para evitar refactorización del código fuente.
     */
    namespace = "com.rosso.harptune"
    compileSdk = 35

    defaultConfig {
        /**
         * ApplicationId: Identificador único de la aplicación en Google Play Console.
         * Configurado según el identificador de la aplicación de frecuencia (detectfreq).
         */
        applicationId = "com.rosso.detectfreq"

        minSdk = 26
        targetSdk = 35

        // Versión incremental para control de despliegue en Play Store
        versionCode = 5
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                // Optimización crítica: C++23 y vectorización con -ffast-math
                cppFlags("-std=c++23", "-O3", "-fno-rtti", "-fno-exceptions", "-ffast-math")
                arguments("-DANDROID_STL=c++_shared")
                abiFilters("arm64-v8a", "x86_64")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }

    buildFeatures {
        compose = true
        prefab = true // Requerido para la integración de Oboe vía AAR
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = libs.versions.cmake.get()
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX Core y Arquitectura
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose (Modern UI)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)

    // Oboe: Motor de audio C++ de baja latencia para Android
    implementation("google.oboe:oboe:1.9.0")

    // Entorno de Pruebas
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}