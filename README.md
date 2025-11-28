## Build and run

Start:

```bash
sudo docker compose up --build -d
```

Stop:

```bash
sudo docker compose down
```

## Rebuild with new database

To stop and remove the volume:

```bash
sudo docker compose down
```

Then rebuild and rerun. The new database will be initializated following `mariadb-init/init.sql` script.

## Adminer

Open http://localhost:8080 with:

- Server: `mariadb`
- Username: `admin`
- Password: `password`
- Database: `uploads`

## Application

Open http://localhost:8081.
