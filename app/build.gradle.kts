plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.flatcode.materialrippleeffect"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.flatcode.materialrippleeffect"
        minSdk = 23
        targetSdk = 37
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":rippleeffect"))
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
}