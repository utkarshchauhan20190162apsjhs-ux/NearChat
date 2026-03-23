# NearChat

NearChat is an offline-first nearby messaging Android app built with Kotlin + Jetpack Compose.

## Highlights
- Bluetooth and Wi-Fi Direct discovery and connection flows
- Real-time style chat UI with local persistence (Room)
- MVVM + StateFlow state management
- Foreground service keeps discovery/transport stable
- Premium dark UI with gradient accents

## Build
```bash
./gradlew :app:assembleDebug
```

## AdSense Setup Snippets
For a companion website/landing page, include the following:

1. Add `ads.txt` at your web root with:
   `google.com, pub-7803324425924506, DIRECT, f08c47fec0942fa0`
2. Add the meta + AdSense script from `web/adsense_head_snippet.html` inside your HTML `<head>`.

## GitHub Pages 404 Fix
If GitHub Pages shows `404 File not found`, ensure:
- `index.html` exists at the published root (this repo now includes one).
- Pages source points to the correct branch/folder.
- Deployment has finished in repository **Settings → Pages**.
