# Docker

## Build and run

Start:

```bash
sudo docker compose up --build -d
```

Stop:

```bash
sudo docker compose stop
```

## Rebuild with new database

To stop and remove the volume:

```bash
sudo docker compose down -v
```

Then rebuild and rerun. The new database will be initializated following `mariadb-init/init.sql` script.

## Adminer

Open http://localhost:8080 with:

- Server: `mariadb`
- Username: `ufoto`
- Password: `4AinfoRep-25`
- Database: `fotoshareDB`

## Application

Open http://localhost:8081.

# VM

After cloning the project:

```bash
mvn clean install
sudo cp /home/adminbt/Fotoshare/target/FotoShare-0.0.1-SNAPSHOT.jar /opt/fotoshare/
sudo -u fotoshare /usr/bin/java -jar /opt/fotoshare/FotoShare-0.0.1-SNAPSHOT.jar
```
