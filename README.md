# Shield TV Browser (prototype)

A small Android TV-friendly browser prototype for Xiaomi Android TV. It includes a remote-friendly address bar, YouTube home shortcut, back navigation, and a very small host-based request filter.

## Important limitations

- This is source code, **not a prebuilt/install-ready APK**.
- The filter is a minimal demonstration, not Brave's filtering engine. It will miss many ads and cannot guarantee ad-free YouTube playback.
- YouTube may not work fully in WebView; DRM, sign-in, playback and site behavior can vary by TV/WebView version.
- Before using this for personal accounts, review the code and security/privacy implications. This prototype has JavaScript enabled for website compatibility.

## Build an APK

1. Upload this folder to a new GitHub repository.
2. Open the repository's **Actions** tab and run **Build Shield TV APK** (or push to `main`).
3. Download the `ShieldTV-debug-apk` artifact from the completed workflow run. Extract `app-debug.apk`.
4. Copy `app-debug.apk` to a USB drive formatted as FAT32 or exFAT.
5. On the TV, use a file manager to open the APK. If Android prompts, allow that file manager to install unknown apps. Keep Play Protect enabled and do not proceed if Android reports a security problem.

## Build locally

Requires Android SDK (platform 35) and Java 17. Run `gradle assembleDebug` from this project folder. APK output: `app/build/outputs/apk/debug/app-debug.apk`.
