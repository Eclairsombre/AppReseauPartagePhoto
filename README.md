## Build and run

Start:

```bash
sudo docker compose up --build
```

Stop:

```bash
sudo docker compose down -v
```

## Rebuild with new database

In case of a database change, stop (using the command above) and delete the volume using:

```bash
sudo docker volume rm appreseaupartagephoto_mariadb_data
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
