# veezee - music streamer (Android)

<p align="center">
	<img width="50%" src="http://veezee.ir/demo_assets/veezee-logotype.svg">
</p>
<br>


**veezee** is a cross-platform music streamer inspired by Apple Music and Spotify for **iOS** and **Android**. It is built with **native** technologies for each platform (**Swift** and **Kotlin**). 

*This is the repository for veezee for **Android**, the **iOS** version can be found [here](https://github.com/veezee-music/veezee-ios).*

<br>

<p align="center">
  <a href="http://veezee.ir/demo_assets/android/p1.png" target="_blank"><img width="32%" src="http://veezee.ir/demo_assets/android/p1.png"></a>
  <a href="http://veezee.ir/demo_assets/android/p2.png" target="_blank"><img width="32%" src="http://veezee.ir/demo_assets/android/p2.png"></a>
  <a href="http://veezee.ir/demo_assets/android/p3.png" target="_blank"><img width="32%" src="http://veezee.ir/demo_assets/android/p3.png"></a>
</p>
<p align="center">
  <a href="http://veezee.ir/demo_assets/android/p4.png" target="_blank"><img width="32%" src="http://veezee.ir/demo_assets/android/p4.png"></a>
  <a href="http://veezee.ir/demo_assets/android/p5.png" target="_blank"><img width="32%" src="http://veezee.ir/demo_assets/android/p5.png"></a>
  <a href="http://veezee.ir/demo_assets/android/p6.png" target="_blank"><img width="32%" src="http://veezee.ir/demo_assets/android/p6.png"></a>
</p>
<p align="center">
  <a href="http://veezee.ir/demo_assets/android/p7.png" target="_blank"><img width="32%" src="http://veezee.ir/demo_assets/android/p7.png"></a>
  <a href="http://veezee.ir/demo_assets/android/p8.png" target="_blank"><img width="32%" src="http://veezee.ir/demo_assets/android/p8.png"></a>
  <a href="http://veezee.ir/demo_assets/android/p9.png" target="_blank"><img width="32%" src="http://veezee.ir/demo_assets/android/p9.png"></a>
</p>




## General features

- Supports tracks, albums, playlists and genres
- User management with email/password and **Google** (Supports cross-device sessions)
- Supports remote (**MongoDB**) and local-offline (**Couchbase**) databases
- **Offline mode**
- **Theme support**
- Sharing

## Android version features

- Completely written in **Kotlin**
- Optimized for all screen aspect ratios
- Uses **ConstraintLayout**
- RxKotlin (Limited use)
- High performance audio streaming engine (using **[ExoPlayer](https://github.com/google/ExoPlayer)**)
- Guest mode (Without login)
- Many custom views for different pages
- Beautiful animations and transitions
- And more...

## Things that are currently NOT planned

- Supporting devices with smaller screens (Minimum for Android is 5 inches)
- Localization
- Equalizer

## How to use

### Compiling: Android version

You'll need to use **Android Studio**.
- Open Android Studio and generate a keystore file that is going to be used to sign the app (for both debug and production builds) ([Instructions](https://stackoverflow.com/questions/3997748/how-can-i-create-a-keystore))
- Rename the `example-gradle.properties` file to `gradle.properties` located in the project root and optionally fill it with API keys for various services supported by veezee
- Open the project by using Android Studio file menu.
- Wait for Android Studio to complete the indexing proccess and then sync Gradle and then build the project.
- Select a device or emulator with a screen size **equal or larger than 5 inch** (e.g. Nexus 5, Google Pixel, Galaxy S9) and run the project

#### Common Android Studio issues

- You may have to remove `fabric.properties` (if it exists) from the `/app` directory and let Gradle rebuild it.
- Be aware of the different results you may get when using quotation marks around values in `gradle.properties` file

### Setting up a server (IMPORTANT!)

**veezee** depends on a functioning HTTPS API server to show music lists and play music as well as do user management and provide analytics data. An incomplete example is provided by the veezee team that can be used as a starting point but it's not completely safe and must be reviewed thoroughly before used in a production environment.

The server can be set up either on the localhost or the Internet. This server's address must be specified in the `Constants.kt` file in the Android application's project code.

*`https://veezee.cloud/api/v1/` is a demo API limited in functionaliity (registrations not allowed) that can be used to understand the apps behavior. **It contains some music and image files that exist for educational purposes only! No profit is gained from them. Its content may not be used in any way that may violate any copyright laws.** This server does not have powerful hardware and network connectivity and is expected to be slow and unresponsive at times.*


**For more information about the server application please visit [here](https://github.com/veezee-music/veezee-server-example).**

## License
veezee (Android) is available under the MIT license. See LICENSE file for more info.
