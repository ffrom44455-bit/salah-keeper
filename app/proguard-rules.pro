# Keep Room schemas
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Hilt / Dagger
-dontwarn dagger.hilt.**
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Keep Adhan library
-keep class com.batoulapps.adhan.** { *; }

# Keep Haze
-keep class dev.chrisbanes.haze.** { *; }
