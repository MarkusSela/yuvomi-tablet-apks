# Container Yuvomi: configurazione verificata

Verifica eseguita sul server Ubuntu il 27 agosto 2026.

## Servizio attivo

- Container: `yuvomi`
- Immagine: `ghcr.io/ulsklyc/yuvomi:2.45.0`
- Comando: `node server/index.js`
- Porta interna: `3000/tcp`
- Stato verificato: attivo e `healthy`
- Endpoint LAN: `http://192.168.1.16:3000`
- Endpoint Tailscale usato dall’APK personale: `https://user-praim-a44.tail6e6024.ts.net:8454`
- Accesso Tailscale: solo tailnet, non Internet pubblico

Tailscale Serve inoltra la porta 8454 verso `192.168.1.16:3000`.

## Compose verificato

Directory sul server: `/home/user/yuvomi`

File Compose: `/home/user/yuvomi/docker-compose.yml`

Il servizio usa:

- `image: ghcr.io/ulsklyc/yuvomi:2.45.0`
- `container_name: yuvomi`
- `restart: unless-stopped`
- bind LAN su `192.168.1.16:${OIKOS_HTTP_PORT:-3000}:3000`
- bind Tailscale su `100.107.29.37:${OIKOS_HTTP_PORT:-3000}:3000`
- `env_file: .env`
- `NODE_ENV=production`
- `DB_PATH` predefinito `/data/yuvomi.db`
- `BACKUP_DIR=/backups`
- healthcheck HTTP su `http://localhost:3000/health`

## Volumi necessari

- `./data:/data`: database e dati applicativi
- `./backups:/backups`: backup
- `./modules:/app/modules`: moduli Yuvomi
- `./documents:/documents` oppure il percorso definito da `DOCUMENT_STORAGE_LOCAL_PATH`: documenti locali, se abilitati

## Configurazione minima

Il file `/home/user/yuvomi/.env` deve esistere sul server e contenere almeno i segreti applicativi richiesti, in particolare `SESSION_SECRET` e `DB_ENCRYPTION_KEY`, oltre alla configurazione della porta e del database. I valori reali non vengono copiati in questa repository.

Per una nuova installazione servono inoltre Docker Engine, Docker Compose v2, directory persistenti per dati e backup, un `.env` generato fuori da Git, Tailscale autenticato sul server e Tailscale Serve configurato per inoltrare 8454 a 3000.

## Controlli prima del tablet

```bash
docker compose -f /home/user/yuvomi/docker-compose.yml ps yuvomi
curl -fsS http://127.0.0.1:3000/health
sudo tailscale serve status
```

Il controllo corretto deve mostrare il container `healthy`, l’endpoint `/health` HTTP 200 e la rotta Tailscale 8454 verso `192.168.1.16:3000`.

## Regola di sicurezza

Non pubblicare la porta 3000 su Internet e non inserire il `.env` nella repository. Il tablet deve raggiungere il servizio tramite LAN autorizzata o Tailscale.
