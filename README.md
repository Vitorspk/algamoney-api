# AlgaMoney API

RESTful API for personal financial management developed with Spring Boot.

## 📋 About the Project

AlgaMoney is a financial control application that allows you to manage:
- **Categories**: Organization of expenses and income
- **People**: Contact management
- **Entries**: Financial transaction records (income and expenses)

## 🚀 Technologies

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **Spring Security** (Basic Auth)
- **MySQL 8.0**
- **Flyway** (Database migrations)
- **Maven**
- **Docker & Docker Compose**

## 📁 Project Structure

```
algamoney-api/
├── docker/                          # Docker files
│   ├── Dockerfile                   # Application image
│   ├── docker-compose.yml           # Service orchestration
│   ├── .dockerignore               # Files ignored in build
│   └── README-DOCKER.md            # Complete Docker guide
├── postman/                         # Postman collection and environments
│   ├── AlgaMoney-API.postman_collection.json
│   ├── AlgaMoney-Docker.postman_environment.json
│   ├── AlgaMoney-Development.postman_environment.json
│   ├── AlgaMoney-Production.postman_environment.json
│   └── POSTMAN-GUIDE.md            # Postman testing guide
├── src/                            # Source code
│   ├── main/
│   │   ├── java/                   # Java classes
│   │   └── resources/              # Configuration files
│   └── test/                       # Tests
├── pom.xml                         # Maven configuration
└── README.md                       # This file
```

## 🐳 Quick Start with Docker

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+

### Initial Setup

1. Configure environment variables:
```bash
cd docker
cp .env.example .env
# Edit the .env file with your credentials
```

2. Run the application:
```bash
docker-compose up -d
```

The application will be available at: http://localhost:8080

**Note:** Database credentials are configured via environment variables for security.

For more details, see: [docker/README-DOCKER.md](docker/README-DOCKER.md)

### Stop the application

```bash
cd docker
docker-compose down
```

## 💻 Local Development

### Prerequisites
- Java 17
- Maven 3.8+
- MySQL 8.0

### Configure database

```sql
CREATE DATABASE algamoneyapi;
```

### Run the application

```bash
./mvnw spring-boot:run
```

## 🔐 Authentication

The API uses **HTTP Basic Authentication**.

### Default Users

| User | Password | Permissions |
|------|----------|------------|
| admin@algamoney.com | admin | Full access |
| maria@algamoney.com | maria | Read only |

## 📡 API Endpoints

### Categories
- `GET /categorias` - List all categories
- `GET /categorias/{id}` - Get category by ID
- `POST /categorias` - Create new category

### People
- `GET /pessoas` - List people (paginated)
- `GET /pessoas/{id}` - Get person by ID
- `POST /pessoas` - Create new person
- `PUT /pessoas/{id}` - Update person
- `PUT /pessoas/{id}/ativo` - Update active status
- `DELETE /pessoas/{id}` - Remove person

### Entries
- `GET /lancamentos` - List entries (paginated)
- `GET /lancamentos?resumo` - Entries summary
- `GET /lancamentos/{id}` - Get entry by ID
- `POST /lancamentos` - Create new entry
- `PUT /lancamentos/{id}` - Update entry
- `DELETE /lancamentos/{id}` - Remove entry

### Health
- `GET /actuator/health` - Application status

## 🧪 Testing with Postman

Import the collection and Postman environments:

1. Open Postman
2. Import files from `postman/` folder:
   - `AlgaMoney-API.postman_collection.json`
   - `AlgaMoney-Docker.postman_environment.json`
3. Select the "AlgaMoney - Docker" environment
4. Execute the requests

For more details, see: [postman/POSTMAN-GUIDE.md](postman/POSTMAN-GUIDE.md)

### Request Example

```bash
# List categories
curl -u admin@algamoney.com:admin http://localhost:8080/categorias

# Create category
curl -u admin@algamoney.com:admin \
  -H "Content-Type: application/json" \
  -d '{"nome":"Transportation"}' \
  http://localhost:8080/categorias
```

## 🗄️ Database

### Flyway Migrations

Migrations are executed automatically on startup:

1. `V01__criar_e_registrar_categorias.sql` - Categories table
2. `V02__criar_e_registrar_pessoas.sql` - People table
3. `V03__criar_e_registrar_lancamentos.sql` - Entries table
4. `V04__criar_e_registrar_usuarios_e_permissoes.sql` - Users and permissions

### Initial Data

**Categories (5)**:
- Leisure
- Food
- Supermarket
- Pharmacy
- Others

**People (1)**:
- João Silva

**Entries (6)**:
- Mix of income and expenses

## 🔧 Configuration

### Spring Profiles

- `basic-security`: Basic authentication (default)
- `docker`: Docker environment settings
- `oauth-security`: OAuth2 (.old files - requires implementation)

### Properties

Main configuration files:
- `application.properties` - Default configuration
- `application-docker.properties` - Docker configuration

## 🚢 Deployment

### Heroku

The application is configured for Heroku deployment:

```bash
git push heroku master
```

### Docker

```bash
cd docker
docker-compose up -d --build
```

## 📝 Logs

### Docker

```bash
cd docker
docker-compose logs -f app      # Application logs
docker-compose logs -f mysql    # MySQL logs
```

### Local Development

Logs are displayed in the console during execution.

## 🛡️ Security

⚠️ **Important**: Current settings are for development/testing.

**For Production:**
- Change default passwords
- Configure HTTPS
- Implement OAuth2/JWT
- Use environment variables for credentials
- Configure CORS appropriately
- Use secrets manager

## 🐛 Troubleshooting

### Application won't start

```bash
# Check logs
cd docker
docker-compose logs app

# Restart services
docker-compose restart
```

### Database connection error

```bash
# Check if MySQL is running
docker-compose ps

# Check MySQL logs
docker-compose logs mysql
```

### Port 8080 in use

Edit `docker/docker-compose.yml`:
```yaml
ports:
  - "8081:8080"  # Use port 8081 externally
```

## 📚 Additional Documentation

- **Docker Setup**: [docker/README-DOCKER.md](docker/README-DOCKER.md)
- **Postman Testing**: [postman/POSTMAN-GUIDE.md](postman/POSTMAN-GUIDE.md)

## 🤝 Contributing

1. Fork the project
2. Create a branch for your feature (`git checkout -b feature/MyFeature`)
3. Commit your changes (`git commit -m 'Add MyFeature'`)
4. Push to the branch (`git push origin feature/MyFeature`)
5. Open a Pull Request

## 📄 License

This project was developed for educational purposes.

## 👥 Authors

- Developed as part of the AlgaWorks course
- Migrated to Spring Boot 3.x and Java 17
- Containerized with Docker

## 🔗 Useful Links

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Flyway](https://flywaydb.org/documentation/)
- [Docker Documentation](https://docs.docker.com/)
- [Postman Documentation](https://learning.postman.com/)

---

**🤖 Project updated with [Claude Code](https://claude.com/claude-code)**
