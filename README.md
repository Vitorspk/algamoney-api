# AlgaMoney API

API RESTful para gerenciamento financeiro pessoal desenvolvida com Spring Boot.

## 📋 Sobre o Projeto

AlgaMoney é uma aplicação de controle financeiro que permite gerenciar:
- **Categorias**: Organização de despesas e receitas
- **Pessoas**: Cadastro de contatos
- **Lançamentos**: Registro de transações financeiras (receitas e despesas)

## 🚀 Tecnologias

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **Spring Security** (Basic Auth)
- **MySQL 8.0**
- **Flyway** (Migrações de banco de dados)
- **Maven**
- **Docker & Docker Compose**

## 📁 Estrutura do Projeto

```
algamoney-api/
├── docker/                          # Arquivos Docker
│   ├── Dockerfile                   # Imagem da aplicação
│   ├── docker-compose.yml           # Orquestração de serviços
│   ├── .dockerignore               # Arquivos ignorados no build
│   └── README-DOCKER.md            # Guia completo do Docker
├── postman/                         # Coleção e ambientes Postman
│   ├── AlgaMoney-API.postman_collection.json
│   ├── AlgaMoney-Docker.postman_environment.json
│   ├── AlgaMoney-Development.postman_environment.json
│   ├── AlgaMoney-Production.postman_environment.json
│   └── POSTMAN-GUIDE.md            # Guia de testes com Postman
├── src/                            # Código fonte
│   ├── main/
│   │   ├── java/                   # Classes Java
│   │   └── resources/              # Arquivos de configuração
│   └── test/                       # Testes
├── pom.xml                         # Configuração Maven
└── README.md                       # Este arquivo
```

## 🐳 Início Rápido com Docker

### Pré-requisitos
- Docker 20.10+
- Docker Compose 2.0+

### Configuração Inicial

1. Configure as variáveis de ambiente:
```bash
cd docker
cp .env.example .env
# Edite o arquivo .env com suas credenciais
```

2. Execute a aplicação:
```bash
docker-compose up -d
```

A aplicação estará disponível em: http://localhost:8080

**Nota:** As credenciais do banco de dados são configuradas via variáveis de ambiente por segurança.

Para mais detalhes, consulte: [docker/README-DOCKER.md](docker/README-DOCKER.md)

### Parar a aplicação

```bash
cd docker
docker-compose down
```

## 💻 Desenvolvimento Local

### Pré-requisitos
- Java 17
- Maven 3.8+
- MySQL 8.0

### Configurar banco de dados

```sql
CREATE DATABASE algamoneyapi;
```

### Executar a aplicação

```bash
./mvnw spring-boot:run
```

## 🔐 Autenticação

A API utiliza **HTTP Basic Authentication**.

### Usuários Padrão

| Usuário | Senha | Permissões |
|---------|-------|------------|
| admin@algamoney.com | admin | Acesso completo |
| maria@algamoney.com | maria | Somente leitura |

## 📡 Endpoints da API

### Categorias
- `GET /categorias` - Listar todas as categorias
- `GET /categorias/{id}` - Buscar categoria por ID
- `POST /categorias` - Criar nova categoria

### Pessoas
- `GET /pessoas` - Listar pessoas (paginado)
- `GET /pessoas/{id}` - Buscar pessoa por ID
- `POST /pessoas` - Criar nova pessoa
- `PUT /pessoas/{id}` - Atualizar pessoa
- `PUT /pessoas/{id}/ativo` - Atualizar status ativo
- `DELETE /pessoas/{id}` - Remover pessoa

### Lançamentos
- `GET /lancamentos` - Listar lançamentos (paginado)
- `GET /lancamentos?resumo` - Resumo de lançamentos
- `GET /lancamentos/{id}` - Buscar lançamento por ID
- `POST /lancamentos` - Criar novo lançamento
- `PUT /lancamentos/{id}` - Atualizar lançamento
- `DELETE /lancamentos/{id}` - Remover lançamento

### Saúde
- `GET /actuator/health` - Status da aplicação

## 🧪 Testes com Postman

Importar a coleção e ambientes do Postman:

1. Abrir Postman
2. Importar arquivos da pasta `postman/`:
   - `AlgaMoney-API.postman_collection.json`
   - `AlgaMoney-Docker.postman_environment.json`
3. Selecionar o ambiente "AlgaMoney - Docker"
4. Executar as requisições

Para mais detalhes, consulte: [postman/POSTMAN-GUIDE.md](postman/POSTMAN-GUIDE.md)

### Exemplo de Requisição

```bash
# Listar categorias
curl -u admin@algamoney.com:admin http://localhost:8080/categorias

# Criar categoria
curl -u admin@algamoney.com:admin \
  -H "Content-Type: application/json" \
  -d '{"nome":"Transporte"}' \
  http://localhost:8080/categorias
```

## 🗄️ Banco de Dados

### Migrações Flyway

As migrações são executadas automaticamente na inicialização:

1. `V01__criar_e_registrar_categorias.sql` - Tabela de categorias
2. `V02__criar_e_registrar_pessoas.sql` - Tabela de pessoas
3. `V03__criar_e_registrar_lancamentos.sql` - Tabela de lançamentos
4. `V04__criar_e_registrar_usuarios_e_permissoes.sql` - Usuários e permissões

### Dados Iniciais

**Categorias (5)**:
- Lazer
- Alimentação
- Supermercado
- Farmácia
- Outros

**Pessoas (1)**:
- João Silva

**Lançamentos (6)**:
- Mix de receitas e despesas

## 🔧 Configuração

### Perfis do Spring

- `basic-security`: Autenticação básica (padrão)
- `docker`: Configurações para ambiente Docker
- `oauth-security`: OAuth2 (arquivos .old - requer implementação)

### Propriedades

Principais arquivos de configuração:
- `application.properties` - Configuração padrão
- `application-docker.properties` - Configuração Docker

## 🚢 Deploy

### Heroku

A aplicação está configurada para deploy no Heroku:

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
docker-compose logs -f app      # Logs da aplicação
docker-compose logs -f mysql    # Logs do MySQL
```

### Desenvolvimento Local

Os logs são exibidos no console durante a execução.

## 🛡️ Segurança

⚠️ **Importante**: As configurações atuais são para desenvolvimento/teste.

**Para Produção:**
- Alterar senhas padrão
- Configurar HTTPS
- Implementar OAuth2/JWT
- Usar variáveis de ambiente para credenciais
- Configurar CORS adequadamente
- Usar gerenciador de segredos

## 🐛 Troubleshooting

### Aplicação não inicia

```bash
# Verificar logs
cd docker
docker-compose logs app

# Reiniciar serviços
docker-compose restart
```

### Erro de conexão com banco

```bash
# Verificar se o MySQL está rodando
docker-compose ps

# Verificar logs do MySQL
docker-compose logs mysql
```

### Porta 8080 em uso

Editar `docker/docker-compose.yml`:
```yaml
ports:
  - "8081:8080"  # Usar porta 8081 externamente
```

## 📚 Documentação Adicional

- **Docker Setup**: [docker/README-DOCKER.md](docker/README-DOCKER.md)
- **Postman Testing**: [postman/POSTMAN-GUIDE.md](postman/POSTMAN-GUIDE.md)

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais.

## 👥 Autores

- Desenvolvido como parte do curso AlgaWorks
- Migrado para Spring Boot 3.x e Java 17
- Containerizado com Docker

## 🔗 Links Úteis

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Flyway](https://flywaydb.org/documentation/)
- [Docker Documentation](https://docs.docker.com/)
- [Postman Documentation](https://learning.postman.com/)

---

**🤖 Projeto atualizado com [Claude Code](https://claude.com/claude-code)**
