# Yuvomi Tablet APK

Repository privata per gli APK Android collegati al container Yuvomi e per la documentazione del tablet.

## Contenuto

- `apks/yuvomi-personale-v0.1.0-corrente.apk`: versione personale già verificata, con endpoint Tailscale di Marco.
- `apks/yuvomi-personale-v0.2.0-debug.apk`: nuova build personale, stesso endpoint, `versionCode 2` / `versionName 0.2.0`.
- `apks/yuvomi-generico-v0.2.0-debug.apk`: build per un altro tablet; al primo avvio chiede l’indirizzo del server Yuvomi.
- `source/personal/`: sorgente del wrapper personale 0.2.0.
- `source/generic/`: sorgente del wrapper generico 0.2.0.
- `original-tablet/README.md`: identificazione e checksum dell’APK originale del tablet.
- `docs/container-yuvomi.md`: configurazione verificata del container Yuvomi.
- `docs/tablet-identico.md`: requisiti e procedura per un tablet identico.
- `checksums/SHA256SUMS.txt`: checksum degli artefatti pubblicati.

L’APK originale `com.fujia.calendar` supera il limite GitHub di 100 MB: viene pubblicato come asset della release privata, con lo stesso SHA-256 indicato nei metadati.

## Scelta dell’APK

### Tablet personale di Marco

Usare `yuvomi-personale-v0.2.0-debug.apk`. Apre direttamente:

`https://user-praim-a44.tail6e6024.ts.net:8454`

La versione personale 0.1.0 corrente resta conservata per rollback e confronto.

### Tablet di un amico

Usare `yuvomi-generico-v0.2.0-debug.apk`. Al primo avvio inserire un endpoint raggiungibile dal tablet, preferibilmente l’URL HTTPS Tailscale del server. L’indirizzo viene salvato localmente nell’app e può essere cambiato dalla schermata di errore.

## Requisiti rapidi del tablet

1. Android 6.0/API 23 o superiore.
2. Tailscale installato e collegato allo stesso tailnet del server.
3. Accesso all’endpoint Yuvomi; la porta Tailscale verificata è `8454`.
4. Installazione manuale dell’APK debug consentita dal sistema.
5. Per l’avvio automatico dopo il boot: consentire avvio automatico e attività in background se il produttore del tablet lo limita.
6. Per upload e download: file picker Android e Download Manager disponibili.

Dettagli completi: `docs/tablet-identico.md`.

## Container Yuvomi

Il servizio verificato sul server usa l’immagine `ghcr.io/ulsklyc/yuvomi:2.45.0`, ascolta sulla porta interna 3000 e viene raggiunto dal tablet tramite Tailscale HTTPS sulla porta 8454. Il container deve essere sano prima di installare o provare l’APK.

Dettagli: `docs/container-yuvomi.md`.

## Build

La build usa un container Android temporaneo con Java, Android SDK 35 e Gradle 8.10.2. Il sistema host non richiede Java o Android SDK installati. Gli APK debug sono destinati al sideload e non sono una release firmata; per aggiornamenti affidabili serve una keystore persistente conservata separatamente.

## Sicurezza

Non inserire in questa repository `.env`, `SESSION_SECRET`, `DB_ENCRYPTION_KEY`, token Tailscale, chiavi GitHub o altri segreti. Il container Yuvomi richiede il proprio `.env` sul server e i valori non fanno parte della documentazione.
