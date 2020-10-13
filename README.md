### About
Double Helix is a live wallpaper and daydream for Android created by [Cypher Cove](http://www.cyphercove.com). 
It is built using the [libGDX framework](https://github.com/libgdx/libgdx).

[![Google Play link](/img/google-play-badge.png)](http://play.google.com/store/apps/details?id=com.cyphercove.doublehelixfree)

All files in this project are Copyright 2015-2020 Cypher Cove LLC. If you have questions about this 
project, you may contact me through the email link at the Google Play storefront.

Double Helix demonstrates the use of my [CoveTools](https://github.com/CypherCove/CoveTools) library
for developing a live wallpaper with libGDX while testing it rapidly on a the desktop. It also 
demonstrates a strategy for using Android Preferences to modify live wallpaper settings and uses
my [CovePrefs](https://github.com/CypherCove/CovePrefs) library for the color picker preference.

### License
The code files of Double Helix are licensed under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0.html), 
which means you may use the code in commercial and non-commercial products. The image and model files 
(files ending in .jpg, .png, and .g3db) are not covered by this license, and are included here for 
demonstration purposes.

You may compile the project as is for your personal use, but do not redistribute it with the image 
and model files, and please don't publish it under the same or similar name (Double Helix). You are 
free to swap these files out and release your own commercial or non-commercial products based on 
this code. If you do create a product based on this code, I appreciate attribution in the application, 
and I'd love to hear about it.

### Building

This project was built using Android Studio. It is a Gradle-based project.

The following libraries are used by Double Helix.

 - [libGDX](http://www.libgdx.com/)
 - [CoveTools](https://github.com/CypherCove/CoveTools)
 - [CovePrefs](https://github.com/CypherCove/CovePrefs)
 - [gdx-tween](https://github.com/CypherCove/gdx-tween)
 - [Timber](https://github.com/JakeWharton/timber)

There is a desktop module included, which allows you to test the live wallpaper on the desktop without 
having to run it on Android. The desktop module can be run using the Gradle `desktop:run` task.

### Legal

Google Play and the Google Play logo are trademarks of Google LLC.