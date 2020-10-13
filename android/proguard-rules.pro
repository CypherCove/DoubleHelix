-verbose
#-printusage android\build\outputs\mapping\playstorePaid\release\.txt

-dontwarn android.support.**
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.physics.box2d.utils.Box2DBuild
-dontwarn com.badlogic.gdx.jnigen.BuildTarget*
-dontwarn com.badlogic.gdx.graphics.g2d.freetype.FreetypeBuild

-keep class com.badlogic.gdx.controllers.android.AndroidControllers

-keepclassmembers class com.badlogic.gdx.backends.android.AndroidInput* {
   <init>(com.badlogic.gdx.Application, android.content.Context, java.lang.Object, com.badlogic.gdx.backends.android.AndroidApplicationConfiguration);
}

-keepnames class com.badlogic.gdx.backends.android.AndroidInput* {
   *;
}

-keepclassmembers class com.badlogic.gdx.physics.box2d.World {
   boolean contactFilter(long, long);
   void    beginContact(long);
   void    endContact(long);
   void    preSolve(long, long);
   void    postSolve(long, long);
   boolean reportFixture(long);
   float   reportRayFixture(long, float, float, float, float, float);
}


-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class androidx.appcompat.app.** { *; }
-keep interface androidx.appcompat.app.** { *; }

-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepnames class com.cyphercove.coveprefs.** { *; }
-keep class com.cyphercove.coveprefs.** { *; }
-keep interface com.cyphercove.coveprefs.** { *; }
-keep class com.cyphercove.audioglow.core.Theme

-dontwarn com.squareup.okhttp.**
-keep class * implements com.cyphercove.covetools.assets.AssetContainer { *; }

-keep class android.arch.lifecycle.** {*;}