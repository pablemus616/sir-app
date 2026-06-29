# kotlinx.serialization — keep @Serializable metadata (the converter relies on generated serializers)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.xnihilfx.sirmobile.** {
    *** Companion;
}
-keepclasseswithmembers class com.xnihilfx.sirmobile.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt/Dagger referencian anotaciones de errorprone que solo existen en tiempo de
# compilación (no en runtime); R8 las marca como "missing classes". Son seguras de ignorar.
-dontwarn com.google.errorprone.annotations.**
