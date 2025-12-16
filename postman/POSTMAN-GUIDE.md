# AlgaMoney API - Postman Testing Guide

## Overview

This guide helps you test the AlgaMoney API using Postman. The API is a financial management system with endpoints for managing categories, people, and financial transactions (income/expenses).

## Quick Start

### 1. Import Collection and Environments

The following files have been created for you:

- **Collection**: `AlgaMoney-API.postman_collection.json`
- **Environments**:
  - `AlgaMoney-Docker.postman_environment.json` (for Docker setup)
  - `AlgaMoney-Development.postman_environment.json` (for local development)
  - `AlgaMoney-Production.postman_environment.json` (for production)

**To import in Postman:**
1. Open Postman
2. Click "Import" button (top left)
3. Drag and drop the collection and environment files
4. Select the appropriate environment from the dropdown (top right)

### 2. Application Status

The application is currently running in Docker:
- **URL**: http://localhost:8080
- **Database**: MySQL 8.0
- **Status**: ✅ Healthy

Check status:
```bash
docker-compose ps
curl -u admin@algamoney.com:admin http://localhost:8080/actuator/health
```

## Authentication

The API uses **HTTP Basic Authentication**. Two users are available:

| Username | Password | Permissions |
|----------|----------|-------------|
| admin@algamoney.com | admin | Full access (all operations) |
| maria@algamoney.com | maria | Read-only access |

Authentication is configured at the collection level, but you can override it per request.

## API Endpoints

### 1. Categorias (Categories)

Manage financial categories (e.g., Lazer, Alimentação, Supermercado).

#### List All Categories
```
GET /categorias
```
**Response**: Array of categories
```json
[
  {
    "codigo": 1,
    "nome": "Lazer"
  }
]
```

#### Get Category by ID
```
GET /categorias/{codigo}
```

#### Create Category
```
POST /categorias
Content-Type: application/json

{
  "nome": "Nova Categoria"
}
```

### 2. Pessoas (People)

Manage people/contacts in the system.

#### Search People (Paginated)
```
GET /pessoas?nome=%&page=0&size=10
```
**Query Parameters:**
- `nome`: Filter by name (% = wildcard)
- `page`: Page number (0-indexed)
- `size`: Items per page

#### Get Person by ID
```
GET /pessoas/{codigo}
```

#### Create Person
```
POST /pessoas
Content-Type: application/json

{
  "nome": "João Silva",
  "ativo": true,
  "endereco": {
    "logradouro": "Rua das Flores",
    "numero": "123",
    "complemento": "Apt 45",
    "bairro": "Centro",
    "cep": "12345-678",
    "cidade": "São Paulo",
    "estado": "SP"
  }
}
```

#### Update Person
```
PUT /pessoas/{codigo}
Content-Type: application/json

{
  "nome": "João Silva Santos",
  "ativo": true,
  "endereco": { ... }
}
```

#### Update Active Status Only
```
PUT /pessoas/{codigo}/ativo
Content-Type: application/json

false
```

#### Delete Person
```
DELETE /pessoas/{codigo}
```

### 3. Lancamentos (Transactions)

Manage financial transactions (income and expenses).

#### Search Transactions (Paginated)
```
GET /lancamentos?page=0&size=10
```
**Optional Query Parameters:**
- `descricao`: Filter by description
- `dataVencimentoDe`: Filter by due date from
- `dataVencimentoAte`: Filter by due date to

#### Get Transaction Summary
```
GET /lancamentos?resumo&page=0&size=10
```
Returns a simplified view of transactions.

#### Get Transaction by ID
```
GET /lancamentos/{codigo}
```

#### Create Transaction (Income)
```
POST /lancamentos
Content-Type: application/json

{
  "descricao": "Salário",
  "dataVencimento": "2025-12-30",
  "dataPagamento": "2025-12-30",
  "valor": 5000.00,
  "tipo": "RECEITA",
  "categoria": {
    "codigo": 1
  },
  "pessoa": {
    "codigo": 1
  },
  "observacao": "Salário mensal"
}
```

#### Create Transaction (Expense)
```
POST /lancamentos
Content-Type: application/json

{
  "descricao": "Conta de Luz",
  "dataVencimento": "2025-12-15",
  "dataPagamento": null,
  "valor": 150.00,
  "tipo": "DESPESA",
  "categoria": {
    "codigo": 2
  },
  "pessoa": {
    "codigo": 1
  },
  "observacao": "Conta mensal"
}
```

**Transaction Types:**
- `RECEITA`: Income
- `DESPESA`: Expense

#### Update Transaction
```
PUT /lancamentos/{codigo}
Content-Type: application/json

{ ... complete transaction object ... }
```

#### Delete Transaction
```
DELETE /lancamentos/{codigo}
```

## Environment Variables

Each environment file contains:

| Variable | Docker | Development | Production |
|----------|---------|-------------|------------|
| base_url | http://localhost:8080 | http://localhost:8080 | https://your-production-url.com |
| username | admin@algamoney.com | maria@algamoney.com | (set your own) |
| password | admin | maria | (set your own) |

## Testing Workflow

### 1. Basic Health Check
1. Select environment: "AlgaMoney - Docker"
2. Run: `Health Check > Application Health`
3. Expected: Status 200, body shows "UP"

### 2. Test Categories
1. Run: `Categorias > List All Categories`
2. Expected: 5 categories (Lazer, Alimentação, etc.)
3. Run: `Categorias > Create Category`
4. Run: `Categorias > List All Categories` again
5. Verify new category appears

### 3. Test People
1. Run: `Pessoas > Create Person`
2. Copy the `codigo` from response
3. Run: `Pessoas > Get Person by ID` (update ID in URL)
4. Run: `Pessoas > Search People`
5. Verify person appears in results

### 4. Test Transactions
1. First, create a person (if not done)
2. Run: `Lancamentos > Create Transaction (Receita)`
3. Run: `Lancamentos > Create Transaction (Despesa)`
4. Run: `Lancamentos > Search Transactions`
5. Run: `Lancamentos > Get Transaction Summary`
6. Verify both transactions appear

## Common Issues & Solutions

### 401 Unauthorized
- **Cause**: Missing or incorrect credentials
- **Solution**: Check username/password in environment or request

### 403 Forbidden
- **Cause**: User doesn't have permission for this operation
- **Solution**: Switch to admin user for write operations

### 404 Not Found
- **Cause**: Resource doesn't exist
- **Solution**: Verify the ID exists by listing resources first

### 400 Bad Request
- **Cause**: Invalid request body or missing required fields
- **Solution**: Check request body matches the examples above

### Connection Refused
- **Cause**: Docker containers not running
- **Solution**:
  ```bash
  docker-compose up -d
  docker-compose ps  # verify status
  ```

## Advanced Features

### Pagination
Most list endpoints support pagination:
```
?page=0&size=10&sort=nome,asc
```
- `page`: Page number (0-indexed)
- `size`: Items per page
- `sort`: Sort field and direction

### Filtering
Transactions support advanced filtering:
```
?descricao=Salario&dataVencimentoDe=2025-01-01&dataVencimentoAte=2025-12-31
```

## Database Seed Data

The database comes pre-populated with:

**Categories (5):**
1. Lazer
2. Alimentação
3. Supermercado
4. Farmacia
5. Outros

**People (1):**
- João Silva (codigo: 1)

**Transactions (6):**
- Mix of income (RECEITA) and expenses (DESPESA)

## API Documentation

For detailed API documentation including all fields and validations, refer to:
- Entity models: `src/main/java/com/example/algamoney/api/model/`
- Controllers: `src/main/java/com/example/algamoney/api/resource/`

## Running Tests in Postman

### Manual Testing
1. Import collection
2. Select environment
3. Click on requests and click "Send"

### Automated Testing (Newman)
Run the entire collection via command line:
```bash
newman run AlgaMoney-API.postman_collection.json \
  -e AlgaMoney-Docker.postman_environment.json \
  --reporters cli,json
```

## Security Notes

⚠️ **Important**: The current setup uses basic authentication for simplicity.

**For Production:**
- Change default passwords
- Implement OAuth2/JWT authentication
- Use HTTPS only
- Store credentials in environment variables or secrets manager
- Enable CORS only for trusted origins

## Support

If you encounter issues:
1. Check Docker containers are running: `docker-compose ps`
2. Check application logs: `docker-compose logs -f app`
3. Verify database connection: `docker-compose logs mysql`
4. Test health endpoint: `curl -u admin@algamoney.com:admin http://localhost:8080/actuator/health`

## Next Steps

1. ✅ Import Postman collection and environments
2. ✅ Test all endpoints
3. Create your own categories, people, and transactions
4. Set up automated tests with Newman
5. Deploy to production and update environment variables

---

**Generated with Claude Code**
For more information about the API, see `README.md` and `README-DOCKER.md`
