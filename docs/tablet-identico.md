# Requirements for an identical tablet

## System

- Android 6.0/API 23 or newer (`minSdk 23`).
- Portrait display orientation.
- System WebView updated through Android System WebView or Chrome.
- Android Download Manager and document picker available.

The wrapper contains no custom native libraries: the build is ABI-independent and uses Android WebView.

## Network

### Personal version

The tablet must have Tailscale installed, be authenticated to the same tailnet as the server, resolve `user-praim-a44.tail6e6024.ts.net`, and reach `https://user-praim-a44.tail6e6024.ts.net:8454`.

### Generic version

At first launch, enter a complete HTTP or HTTPS address without a query or fragment. Recommended example:

`https://server-del-tuo-tailnet.ts.net:8454`

For LAN-only testing, the server address on port 3000 can be used, but Tailscale HTTPS is preferred.

## Installation

1. Install Tailscale and connect the tablet to the tailnet.
2. Download the personal or generic APK from the public release.
3. Temporarily allow installation from the file manager or browser used.
4. Install the APK.
5. Open Yuvomi and verify that the site loads.
6. Disable the installation permission again when it is no longer needed.
7. If launch after reboot is required, allow the app in the manufacturer's auto-start and battery settings.

## Wrapper features

JavaScript, DOM storage/localStorage, WebView database, cookies including third-party cookies, single and multiple file selection, Download Manager downloads, immersive fullscreen, keep-screen-on behavior, WebView history with the back button, and launch after `BOOT_COMPLETED` are enabled.

The implemented fullscreen is the system immersive mode; HTML `onShowCustomView`/`onHideCustomView` handling is not included.

The `⚙️` button opens native Android tablet settings outside the Yuvomi WebView. These settings control brightness, audio, touch sounds, global orientation, display timeout and screensaver access.

## Quick diagnosis

- **Page unreachable:** check Tailscale on the tablet and the container `healthy` status.
- **Certificate/HTTPS:** use the Tailscale hostname instead of an improvised HTTPS IP address.
- **Upload unavailable:** check the Android document picker.
- **Download unavailable:** check Download Manager and available storage.
- **Does not launch after boot:** check manufacturer auto-start and battery-saving permissions.

## Original tablet APK

The original `com.fujia.calendar` APK is retained as a release asset for rollback or recovery. Do not uninstall it from the tablet until the new installation has been verified.
