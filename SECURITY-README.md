# AlgaMoney API - Guia de Segurança

## 🔒 Configuração de Segurança Obrigatória

### Variáveis de Ambiente Requeridas

Antes de executar a aplicação em **produção**, você DEVE configurar as seguintes variáveis de ambiente:

#### 1. JWT Secret (CRÍTICO)

```bash
export JWT_SECRET=$(openssl rand -base64 64)
```

**⚠️ IMPORTANTE:**
- O secret do JWT NÃO possui valor padrão em produção
- A aplicação falhará ao iniciar se JWT_SECRET não estiver configurado
- Use um secret forte de no mínimo 256 bits (44 caracteres em base64)
- NUNCA commite o secret no código ou em arquivos de configuração

#### 2. Database Password (CRÍTICO)

```bash
export SPRING_DATASOURCE_PASSWORD=your_secure_database_password
```

**⚠️ IMPORTANTE:**
- NÃO use senhas padrão como "root" ou "admin" em produção
- O password NÃO possui valor padrão em produção

#### 3. Outras Configurações JWT (Opcional)

```bash
# Tempo de expiração do token (padrão: 30 minutos)
export JWT_EXPIRATION_TIME=1800000

# Issuer (padrão: algamoney-api)
export JWT_ISSUER=algamoney-api

# Audience (padrão: algamoney-ui)
export JWT_AUDIENCE=algamoney-ui
```

---

## 🛠️ Desenvolvimento Local

Para desenvolvimento local, a aplicação usa o perfil `dev` que possui valores padrão:

```bash
# Ativar perfil de desenvolvimento
export SPRING_PROFILES_ACTIVE=dev

# OU adicione ao application-dev.properties
spring.profiles.active=dev
```

**⚠️ Os valores padrão do perfil dev são INSEGUROS e NÃO devem ser usados em produção!**

---

## 📋 Checklist de Segurança para Deploy

Antes de fazer deploy em produção, verifique:

- [ ] `JWT_SECRET` configurado com valor forte (64+ caracteres)
- [ ] `SPRING_DATASOURCE_PASSWORD` configurado com senha segura
- [ ] `SPRING_PROFILES_ACTIVE` NÃO está configurado como `dev`
- [ ] HTTPS está habilitado (via nginx ou similar)
- [ ] Firewall configurado para permitir apenas portas necessárias
- [ ] Database não está exposto publicamente
- [ ] Logs estão sendo monitorados
- [ ] Rate limiting configurado (recomendado)

---

## 🔐 Gerando Secrets Seguros

### JWT Secret

```bash
# Gerar um secret seguro de 512 bits
openssl rand -base64 64

# Exemplo de output:
# vK8s9mP2xQ... (64+ caracteres)
```

### Database Password

```bash
# Gerar uma senha segura de 32 caracteres
openssl rand -base64 32

# Exemplo de output:
# Xp9K2mQ8... (32+ caracteres)
```

---

## 🚨 Indicadores de Problemas de Segurança

A aplicação falhará ao iniciar com mensagens claras se houver problemas de configuração:

### JWT Secret não configurado:
```
CRITICAL SECURITY ERROR: JWT secret is not configured!
Set JWT_SECRET environment variable before starting the application.
```

### JWT Secret muito curto:
```
SECURITY WARNING: JWT secret is too short!
Current length: 16 characters.
Minimum recommended: 32 characters (256 bits).
```

### JWT Secret suspeito:
```
⚠️  JWT secret appears to contain development keywords.
Ensure you're not using a development secret in production!
```

---

## 📊 Logs de Segurança

A aplicação registra os seguintes eventos de segurança:

- ✅ Tentativas de autenticação bem-sucedidas
- ❌ Tentativas de autenticação falhadas
- ⚠️ Falhas de validação JWT
- 🚫 Requisições CORS de origens não autorizadas
- 🔒 Validação de configuração no startup

Monitore estes logs para detectar atividades suspeitas.

---

## 🛡️ Security Headers Configurados

A aplicação aplica automaticamente os seguintes headers de segurança:

- **Content-Security-Policy**: Previne XSS e injeção de conteúdo
- **Strict-Transport-Security (HSTS)**: Força HTTPS por 1 ano
- **X-Frame-Options**: Previne clickjacking
- **X-Content-Type-Options**: Previne MIME sniffing

---

## 📚 Referências

- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)

---

## 🆘 Suporte

Em caso de dúvidas sobre segurança ou configuração:

1. Verifique os logs da aplicação
2. Consulte este documento
3. Revise o código em `src/main/java/com/example/algamoney/api/config/`

