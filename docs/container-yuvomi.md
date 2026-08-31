# Yuvomi container: verified configuration

Verification performed on the Ubuntu server on 27 August 2026.

## Active service

- Container: `yuvomi`
- Image: `ghcr.io/ulsklyc/yuvomi:2.45.0`
- Command: `node server/index.js`
- Internal port: `3000/tcp`
- Verified status: running and `healthy`
- LAN endpoint: `http://192.168.1.16:3000`
- Tailscale endpoint used by the personal APK: `https://user-praim-a44.tail6e6024.ts.net:8454`
- Tailscale access: tailnet only, not the public Internet

Tailscale Serve forwards port 8454 to `192.168.1.16:3000`.

## Verified Compose configuration

Server directory: `/home/user/yuvomi`

Compose file: `/home/user/yuvomi/docker-compose.yml`

The service uses:

- `image: ghcr.io/ulsklyc/yuvomi:2.45.0`
- `container_name: yuvomi`
- `restart: unless-stopped`
- LAN bind on `192.168.1.16:${OIKOS_HTTP_PORT:-3000}:3000`
- Tailscale bind on `100.107.29.37:${OIKOS_HTTP_PORT:-3000}:3000`
- `env_file: .env`
- `NODE_ENV=production`
- default `DB_PATH` `/data/yuvomi.db`
- `BACKUP_DIR=/backups`
- HTTP healthcheck at `http://localhost:3000/health`

## Required volumes

- `./data:/data`: database and application data
- `./backups:/backups`: backups
- `./modules:/app/modules`: Yuvomi modules
- `./documents:/documents` or the path defined by `DOCUMENT_STORAGE_LOCAL_PATH`: local documents, when enabled

## Minimum configuration

The file `/home/user/yuvomi/.env` must exist on the server and contain the required application secrets, especially `SESSION_SECRET` and `DB_ENCRYPTION_KEY`, along with port and database configuration. Actual values are not copied into this repository.

A new installation also requires Docker Engine, Docker Compose v2, persistent data and backup directories, a `.env` generated outside Git, authenticated Tailscale on the server, and Tailscale Serve configured to forward 8454 to 3000.

## Checks before using the tablet

```bash
docker compose -f /home/user/yuvomi/docker-compose.yml ps yuvomi
curl -fsS http://127.0.0.1:3000/health
sudo tailscale serve status
```

The correct result must show the container as `healthy`, HTTP 200 from `/health`, and the Tailscale 8454 route forwarding to `192.168.1.16:3000`.

## Security rule

Do not publish port 3000 to the Internet and do not put `.env` in the repository. The tablet must reach the service through an authorized LAN or Tailscale.
