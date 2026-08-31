# Yuvomi Android Wrapper

Sideloadable APK that opens the complete Yuvomi site through Tailscale HTTPS.

## Behavior

- The UI and logic remain those of the Yuvomi site on the server.
- Default URL: `https://user-praim-a44.tail6e6024.ts.net:8454`.
- JavaScript, cookies, localStorage and service workers enabled.
- Fullscreen mode and keep-screen-on behavior.
- Attachment uploads through the Android file picker.
- Downloads handled by Android Download Manager.
- Back button navigates WebView history.
- Attempt to launch after device boot.
- Native tablet settings are opened by the `⚙️` button and operate directly on Android, outside the Yuvomi WebView.

The tablet must have Tailscale installed and connected to the same tailnet as the server.

## Isolated build

The build uses a temporary Android container; Java and the Android SDK are not installed on the host system. Public APKs must be built as `release`, not `debug`. A signed release requires a persistent keystore and a backup of the key for future updates.
