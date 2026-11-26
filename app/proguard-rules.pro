# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class io.github.tobsef.niimstudio.model.** { *** Companion; }
-keepclasseswithmembers class io.github.tobsef.niimstudio.model.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class io.github.tobsef.niimstudio.model.**$$serializer { *; }

# Cloud catalogue models added in 0.7.3 live in protocol, not model.
-keepclassmembers class io.github.tobsef.niimstudio.protocol.** { *** Companion; }
-keepclasseswithmembers class io.github.tobsef.niimstudio.protocol.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class io.github.tobsef.niimstudio.protocol.**$$serializer { *; }
