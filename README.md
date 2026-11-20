# MovieBox

Android app (Kotlin + Jetpack Compose): Home, Downloads, Me, My List, Web Link, Settings, Watch Options.

## Get the app (no Android Studio needed)
1. Push this code to GitHub. The **Actions** tab builds the app automatically (about 5 minutes).
2. Open the repo's **Releases** page, then **MovieBox latest**, and download `MovieBox.apk`.
3. Open the APK on your phone and install it (allow "install unknown apps" if asked).

You can also start a build by hand: Actions, then Build APK, then Run workflow.

## Where to plug in real features
- `data/Library.kt` has sample data; replace with your API or a Room database.
- `ui/Screens.kt` has `TODO` markers for search, Manage downloads, Watch History, Language and Web Link.
- Downloads "available" space is read from the phone's real storage.
