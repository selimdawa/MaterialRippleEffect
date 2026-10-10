# ProGuard / R8 Rules for Material Ripple Effect

# -------------------------------------------------------------------------
# Keep Material Ripple Effect Library classes
# -------------------------------------------------------------------------
-keep class io.selimdawa.rippleeffect.** { *; }

# -------------------------------------------------------------------------
# Preserve annotations & source attributes for stacktraces and debugging
# -------------------------------------------------------------------------
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes SourceFile, LineNumberTable
