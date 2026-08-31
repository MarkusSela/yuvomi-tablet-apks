# Yuvomi Tablet APK

Repository pubblica per gli APK Android collegati al container Yuvomi e per la documentazione del tablet.

## Stato attuale

La versione aggiornata di sviluppo sorgente è **v0.2.1**. Aggiunge una schermata nativa Android per controllare direttamente il tablet, separata dalla WebView e dal calendario Yuvomi.

La build v0.2.1 deve essere generata in modalità `release` e verificata prima della pubblicazione dell'APK. Gli APK `debug` non vengono conservati nella superficie pubblica.

## Contenuto

- `source/personal/`: sorgente del wrapper personale v0.2.1.
- `source/generic/`: sorgente del wrapper generico v0.2.1.
- `apks/yuvomi-personale-v0.1.0-corrente.apk`: APK personale precedente, conservato per rollback.
- `original-tablet/README.md`: identificazione dell'APK originale del tablet.
- `docs/container-yuvomi.md`: configurazione verificata del container Yuvomi.
- `docs/tablet-identico.md`: requisiti e procedura per un tablet identico.
- `docs/releases/v0.2.1.md`: log delle funzioni native aggiunte.
- `checksums/SHA256SUMS.txt`: checksum degli artefatti ancora pubblici.

## Funzioni v0.2.1

Il pulsante nativo `⚙️` apre **Impostazioni tablet Android**, direttamente nell'APK:

- luminosità manuale e automatica;
- volumi media, sveglie/promemoria, notifiche, suoneria e sistema;
- suoni al tocco;
- orientamento globale automatico, verticale o orizzontale;
- timeout di spegnimento del display;
- lettura dello stato del salvaschermo Android e collegamento alle sue impostazioni;
- richiesta guidata del permesso Android necessario per modificare le impostazioni.

Questi controlli agiscono sul sistema Android e non sulla pagina web del calendario Yuvomi.

## Varianti

### Tablet personale

La variante personale usa l'endpoint configurato per il tablet di Marco. Il tablet deve essere collegato al tailnet Tailscale corretto.

### Tablet generico

La variante generica chiede l'endpoint al primo avvio e lo salva localmente nell'app. L'indirizzo può essere modificato dalla schermata di errore.

## Requisiti

1. Android 6.0/API 23 o superiore.
2. Tailscale collegato allo stesso tailnet del server, quando si usa l'endpoint Tailscale.
3. WebView Android aggiornata.
4. Download Manager e selettore documenti Android disponibili.
5. Per le impostazioni native: autorizzare nell'apposita schermata Android il permesso di modifica delle impostazioni di sistema.

## Container Yuvomi

Il servizio Yuvomi verificato usa un container separato. L'APK è solo un wrapper Android: non contiene il database o i segreti del servizio.

## Build e sicurezza

Le APK pubbliche devono essere build `release`, non `debug`. Le build debug precedenti sono state rimosse dalla repository pubblica. Una release firmata richiede una keystore persistente conservata separatamente; senza keystore non è possibile garantire aggiornamenti Android affidabili.

Non inserire in questa repository `.env`, segreti applicativi, token Tailscale, chiavi GitHub o credenziali.
