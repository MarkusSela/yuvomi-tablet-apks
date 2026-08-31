# Yuvomi Tablet APK

Public repository for the Android APKs connected to the Yuvomi container and for tablet documentation.

## 🚀 Current version

The current source and release are **v0.2.1**. This version adds a native Android settings screen that controls the tablet directly, outside the WebView and the Yuvomi calendar.

## 📦 Contents

- `source/personal/`: source for the personal wrapper v0.2.1.
- `source/generic/`: source for the generic wrapper v0.2.1.
- `apks/yuvomi-personale-v0.1.0-corrente.apk`: previous personal APK retained for rollback.
- `original-tablet/README.md`: identification of the tablet's original APK.
- `docs/container-yuvomi.md`: verified Yuvomi container configuration.
- `docs/tablet-identico.md`: requirements and procedure for an identical tablet.
- `docs/releases/v0.2.1.md`: release log for the native tablet features.
- `checksums/SHA256SUMS.txt`: checksums for public APK artifacts.

The original `com.fujia.calendar` APK is available as an asset of the release because it exceeds GitHub's 100 MB regular-file limit.

## ⚙️ v0.2.1 native tablet features

The native `⚙️` button opens **Android tablet settings** directly inside the APK:

- 💡 manual and automatic brightness;
- 🔊 media, alarm/reminder, notification, ringtone and system volumes;
- 🔔 touch sounds;
- 🔄 global orientation: automatic, portrait or landscape;
- ⏱️ display-off timeout;
- 🖥️ Android screensaver status and settings link;
- 🔐 guided request for the Android settings-modification permission.

These controls modify Android system settings and do not modify the Yuvomi calendar WebView.

## 📱 Variants

### 👤 Personal tablet

Use `yuvomi-personal-v0.2.1.apk` from the [v0.2.1 release](https://github.com/MarkusSela/yuvomi-tablet-apks/releases/tag/v0.2.1). It uses Marco's configured Tailscale endpoint.

The previous v0.1.0 APK remains in the repository for rollback.

### 🌐 Generic tablet

Use `yuvomi-generic-v0.2.1.apk` from the [v0.2.1 release](https://github.com/MarkusSela/yuvomi-tablet-apks/releases/tag/v0.2.1). On first launch, enter the reachable Yuvomi server endpoint; it is stored locally and can be changed from the error screen.

## ✅ Requirements

1. Android 6.0/API 23 or newer.
2. Tailscale connected to the same tailnet as the server when using the Tailscale endpoint.
3. Updated Android WebView.
4. Android Download Manager and document picker available.
5. For native settings, grant the app permission to modify system settings in Android settings.

## 🧩 Yuvomi container

The Yuvomi service runs in a separate container. The APK is only an Android wrapper and does not contain the service database or secrets.

## 🛠️ Build and security

Public APKs are built as `release`, not `debug`, by GitHub Actions when the `v0.2.*` tag workflow runs. The current v0.2.1 release APKs were built successfully by run `33398013795`.

A signed production release requires a persistent keystore stored separately. Do not add `.env` files, application secrets, Tailscale tokens, GitHub keys or credentials to this repository.
