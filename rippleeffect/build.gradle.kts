plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech)
}

android {
    namespace = "io.selimdawa.rippleeffect"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

mavenPublishing {

    coordinates(
        groupId = "io.github.selimdawa", artifactId = "material-ripple-effect", version = "1.0.1"
    )

    publishToMavenCentral(automaticRelease = true)

    signAllPublications()

    pom {
        name.set("Material Ripple Effect")
        description.set("A lightweight, highly customizable Android ripple effect library written in Kotlin.")

        url.set("https://github.com/selimdawa/MaterialRippleEffect")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("selimdawa")
                name.set("Selim Dawa")
                email.set("selimdawa@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/selimdawa/MaterialRippleEffect")
            connection.set("scm:git:https://github.com/selimdawa/MaterialRippleEffect.git")
            developerConnection.set("scm:git:ssh://git@github.com:selimdawa/MaterialRippleEffect.git")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}