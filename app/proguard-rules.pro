# kotlinx.serialization — keep @Serializable metadata (the converter relies on generated serializers)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.xnihilfx.sirmobile.** {
    *** Companion;
}
-keepclasseswithmembers class com.xnihilfx.sirmobile.** {
    kotlinx.serialization.KSerializer serializer(...);
}
