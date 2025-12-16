# AlgaMoney API - Docker Setup

This guide shows how to run the AlgaMoney API application using Docker and Docker Compose.

> **📁 Location**: This file is in `docker/`. All docker-compose commands must be executed from this folder or by specifying the file path: `docker-compose -f docker/docker-compose.yml`

## Prerequisites

- Docker installed (version 20.10 or higher)
- Docker Compose installed (version 2.0 or higher)

To check if they are installed:
```bash
docker --version
docker-compose --version
```

## Architecture

The Docker environment contains:
- **MySQL 8.0**: Database
- **AlgaMoney API**: Spring Boot application running on Java 17

## Configuration

### Environment Variables

For security, database credentials are configured through environment variables.

**Initial setup:**

1. Copy the example file:
```bash
cd docker
cp .env.example .env
```

2. Edit the `.env` file and set your passwords:
```bash
MYSQL_ROOT_PASSWORD=your_password_here
SPRING_DATASOURCE_PASSWORD=your_password_here
```

**Important:**
- The `.env` file is in `.gitignore` and will **never be committed**
- Use strong passwords for production environments
- For local development, you can use simple passwords

## How to use

### 1. Start the application

Navigate to the docker folder and run:

```bash
docker-compose up -d
```

This command will:
1. Build the Docker image of the application
2. Download the MySQL image
3. Create the necessary network and volumes
4. Start the containers

### 2. Follow the logs

To see the application logs:
```bash
docker-compose logs -f app
```

To see MySQL logs:
```bash
docker-compose logs -f mysql
```

To see all logs:
```bash
docker-compose logs -f
```

### 3. Check status

```bash
docker-compose ps
```

You should see something like:
```
NAME                IMAGE               STATUS              PORTS
algamoney-api       algamoney-api       Up (healthy)        0.0.0.0:8080->8080/tcp
algamoney-mysql     mysql:8.0           Up (healthy)        0.0.0.0:3306->3306/tcp
```

### 4. Test the application

The application will be available at:
- **API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

Test with curl:
```bash
# Health check
curl http://localhost:8080/actuator/health

# List categories (requires authentication)
curl -u admin@algamoney.com:admin http://localhost:8080/categorias
```

### 5. Access the database

To connect to MySQL:
```bash
docker exec -it algamoney-mysql mysql -uroot -p algamoneyapi
```

Or use a GUI tool (DBeaver, MySQL Workbench, etc.) with:
- **Host**: localhost
- **Port**: 3306
- **Database**: algamoneyapi
- **Username**: root
- **Password**: <your_password_from_env>

## Useful commands

### Stop containers
```bash
docker-compose stop
```

### Stop and remove containers
```bash
docker-compose down
```

### Stop, remove containers and volumes (deletes database data)
```bash
docker-compose down -v
```

### Rebuild the application
If you made changes to the code:
```bash
docker-compose up -d --build
```

### View real-time logs from a specific service
```bash
docker-compose logs -f app
```

### Execute commands inside the container
```bash
# Shell in the application container
docker exec -it algamoney-api sh

# Shell in the MySQL container
docker exec -it algamoney-mysql bash
```

### Restart a specific service
```bash
docker-compose restart app
```

## Troubleshooting

### Application won't start

1. Check the logs:
   ```bash
   docker-compose logs app
   ```

2. Check if MySQL is healthy:
   ```bash
   docker-compose ps
   ```

3. If MySQL is not healthy, restart:
   ```bash
   docker-compose restart mysql
   ```

### Database connection error

1. Check if MySQL is running:
   ```bash
   docker-compose ps mysql
   ```

2. Check MySQL logs:
   ```bash
   docker-compose logs mysql
   ```

3. Try restarting services:
   ```bash
   docker-compose restart
   ```

### Port 8080 or 3306 already in use

If you already have a service running on these ports, edit the `docker-compose.yml` and change the port mapping:

```yaml
ports:
  - "8081:8080"  # Changes external port to 8081
```

### Clean everything and start from scratch

```bash
# Stop all containers
docker-compose down

# Remove volumes (database data will be lost)
docker-compose down -v

# Remove images
docker rmi algamoney-api_app mysql:8.0

# Start again
docker-compose up -d --build
```

## Environment Variables

Variables are configured through the `.env` file in the `docker/` directory.

**Available variables:**

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password | (required) |
| `MYSQL_DATABASE` | Database name | `algamoneyapi` |
| `SPRING_DATASOURCE_URL` | JDBC connection URL | (configured in .env.example) |
| `SPRING_DATASOURCE_USERNAME` | Database user | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | (required) |
| `JAVA_OPTS` | JVM options | `-Xms256m -Xmx512m` |

**To customize:**

Edit the `docker/.env` file:

```bash
# Adjust JVM memory
JAVA_OPTS=-Xms512m -Xmx1024m

# Use different password
MYSQL_ROOT_PASSWORD=my_secure_password
SPRING_DATASOURCE_PASSWORD=my_secure_password
```

## Security

⚠️ **IMPORTANT**: This setup is for local development environment only!

For production:
- Change default passwords
- Use environment variables or secrets
- Configure SSL/TLS
- Adjust Spring Security settings
- Use a secrets management system (Vault, AWS Secrets Manager, etc.)

## Performance

### Adjust JVM memory

Edit the `JAVA_OPTS` variable in `docker-compose.yml`:
```yaml
JAVA_OPTS: -Xms512m -Xmx1024m
```

### Adjust Docker resources

If necessary, increase Docker Desktop resources at:
- macOS: Docker Desktop → Preferences → Resources
- Windows: Docker Desktop → Settings → Resources

## Default users

After Flyway migrations, the following users will be available:

| Email | Password | Permissions |
|-------|----------|------------|
| admin@algamoney.com | admin | ROLE_CADASTRAR_CATEGORIA, ROLE_PESQUISAR_CATEGORIA, ROLE_CADASTRAR_PESSOA, ROLE_REMOVER_PESSOA, ROLE_PESQUISAR_PESSOA, ROLE_CADASTRAR_LANCAMENTO, ROLE_REMOVER_LANCAMENTO, ROLE_PESQUISAR_LANCAMENTO |
| maria@algamoney.com | maria | ROLE_PESQUISAR_CATEGORIA, ROLE_PESQUISAR_PESSOA, ROLE_PESQUISAR_LANCAMENTO |

## Docker file structure

```
.
├── Dockerfile              # Application image definition
├── docker-compose.yml      # Service orchestration
├── .dockerignore          # Files ignored in build
└── src/main/resources/
    └── application-docker.properties  # Docker environment settings
```

## Next steps

- [ ] Test all API endpoints
- [ ] Validate Flyway migrations
- [ ] Configure CORS if necessary
- [ ] Implement OAuth2 Authorization Server (.old files)
- [ ] Add nginx as reverse proxy (optional)
- [ ] Configure centralized logs (ELK Stack, etc.)
