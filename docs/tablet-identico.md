# Requisiti per un tablet identico

## Sistema

- Android 6.0/API 23 o superiore (`minSdk 23`).
- Schermo in orientamento verticale: il wrapper dichiara portrait.
- WebView di sistema aggiornato tramite Android System WebView o Chrome.
- Download Manager e selettore documenti Android disponibili.

Il wrapper non contiene librerie native proprie: la build è indipendente dall’ABI del tablet e usa la WebView Android.

## Rete

### Versione personale

Il tablet deve avere Tailscale installato, essere autenticato nello stesso tailnet del server, risolvere `user-praim-a44.tail6e6024.ts.net` e raggiungere `https://user-praim-a44.tail6e6024.ts.net:8454`.

### Versione generica

Al primo avvio inserire un indirizzo completo HTTP o HTTPS, senza query o frammento. Esempio raccomandato:

`https://server-del-tuo-tailnet.ts.net:8454`

Per il test solo LAN è possibile usare l’indirizzo del server sulla porta 3000, ma Tailscale HTTPS è la scelta preferita.

## Installazione

1. Installare Tailscale e collegare il tablet al tailnet.
2. Scaricare l’APK personale o generico dalla release privata.
3. Abilitare temporaneamente l’installazione da sorgenti consentite dal file manager/browser usato.
4. Installare l’APK.
5. Aprire Yuvomi e verificare il caricamento del sito.
6. Disabilitare nuovamente l’autorizzazione di installazione se non serve.
7. Se è richiesto l’avvio dopo il riavvio, autorizzare l’app nelle impostazioni di avvio automatico/batteria del produttore.

## Funzioni del wrapper

Sono abilitate JavaScript, DOM storage/localStorage e database WebView, cookie inclusi quelli di terze parti, selezione di uno o più file, download tramite Download Manager, modalità immersive fullscreen, schermo mantenuto acceso, cronologia WebView con tasto indietro e avvio dopo `BOOT_COMPLETED`.

Il fullscreen implementato è quello immersivo del sistema; non è presente la gestione HTML `onShowCustomView`/`onHideCustomView`.

## Diagnosi rapida

- **Pagina non raggiungibile:** controllare Tailscale sul tablet e lo stato `healthy` del container.
- **Certificato/HTTPS:** usare l’hostname Tailscale e non un IP HTTPS improvvisato.
- **Upload assente:** verificare il selettore documenti Android.
- **Download assente:** verificare Download Manager e spazio disponibile.
- **Non parte dopo il boot:** verificare permessi di avvio automatico e risparmio batteria del produttore.

## APK originale del tablet

L’APK originale `com.fujia.calendar` è conservato come asset della release per rollback o ripristino. Non disinstallarlo dal tablet finché la nuova installazione non è stata verificata.
