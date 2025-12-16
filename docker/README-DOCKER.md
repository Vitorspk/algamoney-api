# AlgaMoney API - Docker Setup

Este guia mostra como executar a aplicação AlgaMoney API usando Docker e Docker Compose.

> **📁 Localização**: Este arquivo está em `docker/`. Todos os comandos docker-compose devem ser executados desta pasta ou especificando o caminho do arquivo: `docker-compose -f docker/docker-compose.yml`

## Pré-requisitos

- Docker instalado (versão 20.10 ou superior)
- Docker Compose instalado (versão 2.0 ou superior)

Para verificar se estão instalados:
```bash
docker --version
docker-compose --version
```

## Arquitetura

O ambiente Docker contém:
- **MySQL 8.0**: Banco de dados
- **AlgaMoney API**: Aplicação Spring Boot rodando em Java 17

## Configuração

### Variáveis de Ambiente

Por segurança, as credenciais do banco de dados são configuradas através de variáveis de ambiente.

**Setup inicial:**

1. Copie o arquivo de exemplo:
```bash
cd docker
cp .env.example .env
```

2. Edite o arquivo `.env` e defina suas senhas:
```bash
MYSQL_ROOT_PASSWORD=sua_senha_aqui
SPRING_DATASOURCE_PASSWORD=sua_senha_aqui
```

**Importante:**
- O arquivo `.env` está no `.gitignore` e **nunca será commitado**
- Use senhas fortes para ambientes de produção
- Para desenvolvimento local, você pode usar senhas simples

## Como usar

### 1. Subir a aplicação

Navegue até a pasta docker e execute:

```bash
docker-compose up -d
```

Este comando irá:
1. Construir a imagem Docker da aplicação
2. Baixar a imagem do MySQL
3. Criar a rede e volumes necessários
4. Iniciar os containers

### 2. Acompanhar os logs

Para ver os logs da aplicação:
```bash
docker-compose logs -f app
```

Para ver os logs do MySQL:
```bash
docker-compose logs -f mysql
```

Para ver todos os logs:
```bash
docker-compose logs -f
```

### 3. Verificar status

```bash
docker-compose ps
```

Você deve ver algo como:
```
NAME                IMAGE               STATUS              PORTS
algamoney-api       algamoney-api       Up (healthy)        0.0.0.0:8080->8080/tcp
algamoney-mysql     mysql:8.0           Up (healthy)        0.0.0.0:3306->3306/tcp
```

### 4. Testar a aplicação

A aplicação estará disponível em:
- **API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

Teste com curl:
```bash
# Health check
curl http://localhost:8080/actuator/health

# Listar categorias (requer autenticação)
curl -u admin@algamoney.com:admin http://localhost:8080/categorias
```

### 5. Acessar o banco de dados

Para conectar ao MySQL:
```bash
docker exec -it algamoney-mysql mysql -uroot -p  algamoneyapi
```

Ou use uma ferramenta GUI (DBeaver, MySQL Workbench, etc.) com:
- **Host**: localhost
- **Port**: 3306
- **Database**: algamoneyapi
- **Username**: root
- **Password**: 

## Comandos úteis

### Parar os containers
```bash
docker-compose stop
```

### Parar e remover os containers
```bash
docker-compose down
```

### Parar, remover containers e volumes (apaga os dados do banco)
```bash
docker-compose down -v
```

### Rebuild da aplicação
Se você fez alterações no código:
```bash
docker-compose up -d --build
```

### Ver logs em tempo real de um serviço específico
```bash
docker-compose logs -f app
```

### Executar comandos dentro do container
```bash
# Shell no container da aplicação
docker exec -it algamoney-api sh

# Shell no container do MySQL
docker exec -it algamoney-mysql bash
```

### Reiniciar um serviço específico
```bash
docker-compose restart app
```

## Troubleshooting

### A aplicação não inicia

1. Verifique os logs:
   ```bash
   docker-compose logs app
   ```

2. Verifique se o MySQL está saudável:
   ```bash
   docker-compose ps
   ```

3. Se o MySQL não estiver healthy, reinicie:
   ```bash
   docker-compose restart mysql
   ```

### Erro de conexão com banco de dados

1. Verifique se o MySQL está rodando:
   ```bash
   docker-compose ps mysql
   ```

2. Verifique os logs do MySQL:
   ```bash
   docker-compose logs mysql
   ```

3. Tente reiniciar os serviços:
   ```bash
   docker-compose restart
   ```

### Porta 8080 ou 3306 já está em uso

Se você já tem algum serviço rodando nessas portas, edite o `docker-compose.yml` e altere o mapeamento de portas:

```yaml
ports:
  - "8081:8080"  # Muda a porta externa para 8081
```

### Limpar tudo e começar do zero

```bash
# Para todos os containers
docker-compose down

# Remove volumes (dados do banco serão perdidos)
docker-compose down -v

# Remove imagens
docker rmi algamoney-api_app mysql:8.0

# Sobe novamente
docker-compose up -d --build
```

## Variáveis de Ambiente

As variáveis são configuradas através do arquivo `.env` no diretório `docker/`.

**Variáveis disponíveis:**

| Variável | Descrição | Valor Padrão |
|----------|-----------|--------------|
| `MYSQL_ROOT_PASSWORD` | Senha do root do MySQL | (obrigatório) |
| `MYSQL_DATABASE` | Nome do banco de dados | `algamoneyapi` |
| `SPRING_DATASOURCE_URL` | URL de conexão JDBC | (configurado no .env.example) |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | (obrigatório) |
| `JAVA_OPTS` | Opções da JVM | `-Xms256m -Xmx512m` |

**Para customizar:**

Edite o arquivo `docker/.env`:

```bash
# Ajustar memória da JVM
JAVA_OPTS=-Xms512m -Xmx1024m

# Usar senha diferente
MYSQL_ROOT_PASSWORD=minha_senha_segura
SPRING_DATASOURCE_PASSWORD=minha_senha_segura
```

## Segurança

⚠️ **IMPORTANTE**: Este setup é para ambiente de desenvolvimento local apenas!

Para produção:
- Mude as senhas padrão
- Use variáveis de ambiente ou secrets
- Configure SSL/TLS
- Ajuste as configurações de segurança do Spring Security
- Use um sistema de gerenciamento de segredos (Vault, AWS Secrets Manager, etc.)

## Performance

### Ajustar memória da JVM

Edite a variável `JAVA_OPTS` no `docker-compose.yml`:
```yaml
JAVA_OPTS: -Xms512m -Xmx1024m
```

### Ajustar recursos do Docker

Se necessário, aumente os recursos do Docker Desktop em:
- macOS: Docker Desktop → Preferences → Resources
- Windows: Docker Desktop → Settings → Resources

## Usuarios padrão

Após as migrações do Flyway, os seguintes usuários estarão disponíveis:

| Email | Senha | Permissões |
|-------|-------|------------|
| admin@algamoney.com | admin | ROLE_CADASTRAR_CATEGORIA, ROLE_PESQUISAR_CATEGORIA, ROLE_CADASTRAR_PESSOA, ROLE_REMOVER_PESSOA, ROLE_PESQUISAR_PESSOA, ROLE_CADASTRAR_LANCAMENTO, ROLE_REMOVER_LANCAMENTO, ROLE_PESQUISAR_LANCAMENTO |
| maria@algamoney.com | maria | ROLE_PESQUISAR_CATEGORIA, ROLE_PESQUISAR_PESSOA, ROLE_PESQUISAR_LANCAMENTO |

## Estrutura de arquivos Docker

```
.
├── Dockerfile              # Definição da imagem da aplicação
├── docker-compose.yml      # Orquestração dos serviços
├── .dockerignore          # Arquivos ignorados no build
└── src/main/resources/
    └── application-docker.properties  # Configurações para ambiente Docker
```

## Próximos passos

- [ ] Testar todos os endpoints da API
- [ ] Validar as migrações do Flyway
- [ ] Configurar CORS se necessário
- [ ] Implementar OAuth2 Authorization Server (arquivos .old)
- [ ] Adicionar nginx como reverse proxy (opcional)
- [ ] Configurar logs centralizados (ELK Stack, etc.)
