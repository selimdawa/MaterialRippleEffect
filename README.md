# Material Ripple Effect 🌊

A lightweight, highly customizable **Android library** written in **100% Kotlin** to add beautiful, smooth Material Design ripple touch effects to any view in your application.

<p align="center">
 <a><img alt="Min SDK" src="https://img.shields.io/badge/Min SDK-23-020290?logo=android&logoColor=white"/></a>
 <a><img alt="Target SDK" src="https://img.shields.io/badge/Target SDK-37-0EB265?logo=android&logoColor=0EB265"/></a>
 <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.0-blue?logo=kotlin&logoColor=white"/></a>
</p>

---

## ✨ Features

- **Easy Integration:** Add modern ripple feedback with just a few lines of code or XML.
- **Highly Customizable:** Control ripple color, duration, alpha, and radius.
- **Masking Support:** Fits perfectly within rounded corners, circles, or custom shapes.
- **100% Kotlin:** Optimized for modern Android development.

---

## 📦 Installation

### 1. Add the Repository
Add it in your root `build.gradle.kts` at the end of repositories:

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
Add the dependency to your app-level `build.gradle.kts` file:


```kotlin
dependencies {
    implementation("io.github.selimdawa:material-ripple-effect:x.y.z")
}
```


---

## 🚀 Usage

### 1. XML Layout Implementation
You can wrap any view with the ripple layout directly inside your XML layout:

```xml
<com.selimdawa.materialrippleeffect.MaterialRippleLayout
    android:id="@+id/rippleLayout"
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

</com.selimdawa.materialrippleeffect.MaterialRippleLayout>
```

### 2. Programmatic Implementation (Kotlin)

For complete control, you can initialize, configure, or dynamically trigger the ripple effect directly via Kotlin code.

#### A. Basic Configuration
Modify the visual properties of your ripple layout on the fly:

```kotlin
import android.graphics.Color
import com.selimdawa.materialrippleeffect.MaterialRippleLayout

val rippleLayout = findViewById<MaterialRippleLayout>(R.id.rippleLayout)

rippleLayout.apply {
    setRippleColor(Color.parseColor("#6200EE")) // Change wave color
    setRippleDuration(400)                     // Duration in milliseconds
    setRippleAlpha(0.25f)                      // Opacity between 0.0f and 1.0f
    setRippleRadius(16f)                       // Custom mask corner radius
}
```

#### B. Programmatic Initialization
If you are building your UI purely in code without XML layout files, you can wrap any child view programmatically:

```kotlin
// Create your target view (e.g., an ImageView)
val myImageView = ImageView(context).apply {
    setImageResource(R.drawable.my_image)
}

// Wrap it inside the MaterialRippleLayout
val dynamicRipple = MaterialRippleLayout(context).apply {
    setRippleColor(Color.WHITE)
    setRippleDuration(250)
    // Add the view inside the ripple layout container
    addView(myImageView) 
}

// Add 'dynamicRipple' to your parent root layout
rootLayout.addView(dynamicRipple)
```

#### C. Handling Click Events & Callbacks
To prevent the ripple from interrupting standard tap triggers, you can set a safe click listener that executes *after* the animation finishes or immediately:

```kotlin
rippleLayout.setOnClickListener {
    // Standard click action triggered alongside the ripple start
    println("Ripple started!")
}

// Optional: Advanced callback for fine-grained animation control
rippleLayout.setOnRippleCompleteListener {
    // Safely execute logic ONLY after the ripple wave completely disappears
    navigateToNextScreen()
}
```

#### D. Kotlin Extension Function (Pro-Tip 💡)
To make your codebase incredibly clean, you can write an extension function to wrap any view in your project instantly:

```kotlin
fun View.withRipple(color: Int = Color.GRAY, duration: Int = 300): MaterialRippleLayout {
    val parentLayout = MaterialRippleLayout(this.context).apply {
        setRippleColor(color)
        setRippleDuration(duration)
    }
    
    // Remap structural layout params if needed
    parentLayout.layoutParams = this.layoutParams
    
    parentLayout.addView(this)
    return parentLayout
}

// Usage in your Activity or Fragment:
val modernButton = myStandardButton.withRipple(Color.RED, 350)
```

---

## ⚙️ Custom Attributes & API Mapping

Configure your ripple layout with granular precision. This reference outlines every structural, behavioral, performance, and visual attribute available within the layout ecosystem.

### Attribute Reference Table

| XML Attribute | Description |
| :--- | :--- |
| `app:mrl_rippleOverlay` | Draws the ripple on top of child views when true. |
| `app:mrl_rippleColor` | Sets the custom color of the moving ripple wave. |
| `app:mrl_rippleAlpha` | Controls the maximum transparency layer from 0.0 to 1.0. |
| `app:mrl_rippleDimension` | Sets the starting radius of the initial touch ripple. |
| `app:mrl_rippleHover` | Enables a subtle background fade layer on view touch. |
| `app:mrl_rippleRoundedCorners` | Shapes the ripple corners to match rounded layout nodes. |
| `app:mrl_rippleInAdapter` | Optimizes animation performance inside heavy ListView/RecyclerView items. |
| `app:mrl_rippleDuration` | Sets the total wave expansion speed in milliseconds. |
| `app:mrl_rippleFadeDuration` | Sets the final exit fade speed on finger release. |
| `app:mrl_rippleDelayClick` | Delays action triggers to ensure the wave is visible. |
| `app:mrl_rippleBackground` | Adds a clean background color beneath the ripple canvas. |
| `app:mrl_ripplePersistent` | Keeps the final ripple background active after animation ends. |

---

### Core Behavioral Matrix (How Attributes Interact)

To avoid visual glitches, keep these critical rendering rules in mind when combining attributes:

* **`mre_boundless="true"` vs `mre_rippleRadius`:** If `mre_boundless` is enabled, any `mre_rippleRadius` configuration is automatically ignored. Since boundless ripples are uncapped and have no boundaries, clipping mask edges cannot be calculated or applied.
* **`mre_rippleOverlay="true"` Warning:** When drawing the ripple directly on top of children, ensure your `mre_rippleAlpha` is kept extremely low (`< 0.15`). High opacity values will completely cover up text elements, icons, and readability during the press window.
* **Performance Note (`mre_rippleRadius`):** Setting a custom radius utilizes native hardware acceleration paths (`Canvas.clipPath`). For long lists like `RecyclerView`, keep your corner configurations consistent across viewholders to optimize structural view recycling.


### Detailed XML Breakdown & Usage Examples

#### 1. Customizing Alpha & Speed (Subtle & Fast)
Perfect for dark-themed interfaces where default ripples might look overly bright or intrusive.
```xml
<com.selimdawa.materialrippleeffect.MaterialRippleLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:mre_rippleColor="@color/purple_200"
    app:mre_rippleDuration="150" 
    app:mre_rippleAlpha="0.08"> <!-- Super subtle transparency -->

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Fast Subtle Ripple" />

</com.selimdawa.materialrippleeffect.MaterialRippleLayout>
```

#### 2. Matching Rounded Corner Views (Masking)
When your child view has rounded corners (like a custom card, pill button, or image layout), use `mre_rippleRadius` to cleanly mask the wave animation inside those boundaries.
```xml
<com.selimdawa.materialrippleeffect.MaterialRippleLayout
    android:layout_width="200dp"
    android:layout_height="100dp"
    app:mre_rippleColor="#FF5722"
    app:mre_rippleRadius="24dp"> <!-- Matches the background corner radius -->

    <View
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/rounded_button_background" />

</com.selimdawa.materialrippleeffect.MaterialRippleLayout>
```

#### 3. Boundless Action Icons
Ideal for small toolbar icons or standalone system buttons where the touch feedback is expected to spread past the square dimensions of the target icon.
```xml
<com.selimdawa.materialrippleeffect.MaterialRippleLayout
    android:layout_width="48dp"
    android:layout_height="48dp"
    app:mre_rippleColor="?attr/colorControlHighlight"
    app:mre_boundless="true"> <!-- Spreads out outside the layout bounds -->

    <ImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/ic_favorite"
        android:layout_gravity="center" />

</com.selimdawa.materialrippleeffect.MaterialRippleLayout>
```

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
