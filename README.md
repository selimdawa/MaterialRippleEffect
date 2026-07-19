# Material Ripple Effect 🌊

A lightweight, highly customizable **Android library** written in **100% Kotlin** to add beautiful, smooth Material Design ripple touch effects to any view in your application.

<p style="text-align: center;">
 <a><img alt="Min SDK" src="https://img.shields.io/badge/Min SDK-23-020290?logo=android&logoColor=white"/></a>
 <a><img alt="Target SDK" src="https://img.shields.io/badge/Target SDK-37-0EB265?logo=android&logoColor=0EB265"/></a>
 <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.0-blue?logo=kotlin&logoColor=white"/></a>
</p>

---

## ✨ Features

- **Easy Integration:** Add modern ripple feedback with just a few lines of code or XML.
- **Highly Customizable:** Control ripple color, duration, alpha, and mask radius.
- **Kotlin DSL:** Optimized for modern Android development with a clean, idiomatic DSL.
- **Adapter Optimized:** High-performance rendering inside `RecyclerView` and `ListView`.
- **Material 3 Ready:** Complements modern Material Design components perfectly.

---

## 📦 Installation

### 1. Add the Repository
Add it in your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

### 2. Add the Dependency
Add the dependency to your app-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.selimdawa:material-ripple-effect:x.y.z")
}
```

---

## 🚀 Usage

### 1. XML Layout Implementation
Wrap any view with `MaterialRippleEffect` directly in your XML:

```xml
<io.selimdawa.rippleeffect.MaterialRippleEffect
    android:id="@+id/rippleEffect"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:mrl_rippleColor="#FF0000"
    app:mrl_rippleDuration="350"
    app:mrl_rippleAlpha="0.2"
    app:mrl_rippleRoundedCorners="12dp">

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Click Me" />

</io.selimdawa.rippleeffect.MaterialRippleEffect>
```

### 2. Idiomatic Kotlin DSL
Apply ripple effects to any existing view programmatically using the DSL:

```kotlin
import android.graphics.Color
import io.selimdawa.rippleeffect.materialRipple

// Just call .materialRipple on any View
myButton.materialRipple {
    rippleColor(Color.parseColor("#6200EE")) 
    rippleDuration(400)                     
    rippleAlpha(0.25f)                      
    rippleRoundedCorners(16) // radius in dp
}
```

---

## ⚙️ Custom Attributes

Configure your ripple effect with granular precision.

### Attribute Reference Table

| XML Attribute                  | Description                                                    |
|:-------------------------------|:---------------------------------------------------------------|
| `app:mrl_rippleOverlay`        | Draws the ripple on top of child views when true.              |
| `app:mrl_rippleColor`          | Sets the custom color of the moving ripple wave.               |
| `app:mrl_rippleAlpha`          | Controls the maximum transparency layer from 0.0 to 1.0.       |
| `app:mrl_rippleDimension`      | Sets the starting radius of the initial touch ripple.          |
| `app:mrl_rippleHover`          | Enables a subtle background fade layer on view touch.          |
| `app:mrl_rippleRoundedCorners` | Shapes the ripple corners to match rounded layout nodes.       |
| `app:mrl_rippleInAdapter`      | Optimizes animation performance inside ListView/RecyclerView.  |
| `app:mrl_rippleDuration`       | Sets the total wave expansion speed in milliseconds.           |
| `app:mrl_rippleFadeDuration`   | Sets the final exit fade speed on finger release.              |
| `app:mrl_rippleDelayClick`     | Delays action triggers to ensure the wave is visible.          |
| `app:mrl_rippleBackground`     | Adds a background color beneath the ripple canvas.             |
| `app:mrl_ripplePersistent`     | Keeps the final ripple background active after animation ends. |

---

## 🛠️ Requirements

- **Minimum SDK:** 23 (Android 6.0)
- **Target SDK:** 37
- **Language:** Kotlin (100%)

---

## 🤝 Contributing

Contributions are welcome! If you find any bugs or have feature requests, feel free to open an **Issue** or submit a **Pull Request**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

```text
Copyright 2026 Selim Dawa

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
