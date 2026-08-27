# Yuvomi Android Wrapper

APK sideloadabile che apre il sito Yuvomi completo tramite Tailscale HTTPS.

## Comportamento

- La UI e la logica restano quelle del sito Yuvomi sul server.
- URL predefinito: `https://user-praim-a44.tail6e6024.ts.net:8454`.
- JavaScript, cookie, localStorage e service worker abilitati.
- Fullscreen e schermo mantenuto acceso.
- Upload allegati tramite selettore file Android.
- Download gestiti dal Download Manager Android.
- Tasto indietro interno alla cronologia WebView.
- Tentativo di apertura dopo il boot del dispositivo.

Il tablet deve avere Tailscale installato e collegato allo stesso tailnet del server.

## Build isolata

La build usa un container Android temporaneo; Java e Android SDK non vengono installati
sul sistema host. L'APK di debug è adatto al primo test di sideload. Una release firmata
richiede una keystore persistente e un backup della chiave per gli aggiornamenti futuri.
