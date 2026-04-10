# ==========================================
# GSON RULES (Fixes TypeToken Crash)
# ==========================================
# Preserve generic signatures, inner classes (critical for TypeToken), and annotations
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Keep Gson internal classes safe
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# CRITICAL FIX: Keep all anonymous inner classes that extend TypeToken
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * extends com.google.gson.reflect.TypeToken { *; }

# Keep standard serializable configurations
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ==========================================
# PRESERVE YOUR DATA MODELS
# ==========================================
# This protects the actual model classes inside your TypeToken from being renamed!
-keep class com.byteswiz.parentmodel.** { *; }
-keep class com.byteswiz.shoppazingkds.models.** { *; }

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE


# ==========================================
# JAVA SOCKET SERIALIZATION RULES
# ==========================================
# R8 MUST NOT rename or shrink any classes sent over ObjectOutputStream,
# otherwise the receiving KDS app cannot deserialize them.
-keep class com.byteswiz.parentmodel.** { *; }
-keepnames class com.byteswiz.parentmodel.** { *; }

# Keep standard serializable configurations
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}