# Sistema Dunamis

Sistema web para gestão da **Igreja Evangélica Dunamis de Jesus Cristo** e da **Escola Bíblica do Pregador Dunamis**.

## Stack

- Java 17
- Spring Boot 3.2
- Spring Web, Data JPA, Security, Validation
- MySQL
- Thymeleaf + Bootstrap 5
- Maven

## Pré-requisitos

- JDK 17+
- Maven 3.9+
- MySQL 8+ (para desenvolvimento com perfil `dev`)

## Estrutura do projeto

```
src/main/java/com/dunamis/sistema/
├── SistemaDunamisApplication.java
├── config/          # Configurações (Security, Web, App)
├── controller/
│   ├── web/         # Páginas MVC
│   └── api/         # REST (quando necessário)
├── service/         # Regras de negócio
├── repository/      # Spring Data JPA
├── entity/          # Entidades JPA
├── dto/             # DTOs de entrada/saída
├── mapper/          # Conversão entity ↔ DTO
├── security/        # Autenticação e autorização
└── exception/       # Tratamento global de erros
```

## Configuração local

1. Copie `.env.example` para `.env` (não commitar o `.env`).
2. Crie a base de dados MySQL ou use a URL com `createDatabaseIfNotExist=true`.
3. Defina as variáveis de ambiente:

| Variável | Descrição | Exemplo |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil Spring | `dev` |
| `PORT` | Porta do servidor | `8080` |
| `DB_URL` | JDBC MySQL | `jdbc:mysql://localhost:3306/dunamis_db?...` |
| `DB_USERNAME` | Utilizador MySQL | `root` |
| `DB_PASSWORD` | Password MySQL | *(vazio ou sua password)* |

## Executar localmente

> **Requisito:** JDK 17 instalado. Defina `JAVA_HOME` apontando para o JDK (ex: `C:\Program Files\Java\jdk-17`).

O projeto inclui **Maven Wrapper** (`mvnw` / `mvnw.cmd`) — não é necessário instalar Maven globalmente.

```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.18
mvnw.cmd clean package -DskipTests

# Executar
mvnw.cmd spring-boot:run

# Ou via JAR
java -jar target/sistema-dunamis-0.1.0-SNAPSHOT.jar
```

```bash
# Linux / macOS
export JAVA_HOME=/path/to/jdk-17
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

## Modelo de dados (Fase 2)

### Entidades

| Entidade | Tabela | Descrição |
|---|---|---|
| `User` | `users` | Conta + perfil do membro (contacto único para login) |
| `Role` | `roles` | Papéis: ADMIN, MEMBER, STUDENT |
| `ChurchFunction` | `church_functions` | Funções na igreja (extensível) |
| `Turma` | `turmas` | Turma da Escola Bíblica |
| `Disciplina` | `disciplinas` | Disciplinas por turma |
| `Inscricao` | `inscricoes` | Inscrição membro ↔ turma (perfil aluno) |
| `Nota` | `notas` | Notas por inscrição e disciplina |
| `Presenca` | `presencas` | Presenças por inscrição e data |

### Dados iniciais (seed automático)

Ao iniciar a aplicação, são criados automaticamente (se não existirem):

- Roles: `ROLE_ADMIN`, `ROLE_MEMBER`, `ROLE_STUDENT`
- Funções na igreja: Pastor, Diácono, Louvor, Intercessão, Evangelismo, Jovens, Crianças, Outro, Sem função
- Turma ativa padrão do ano corrente

### Verificar tabelas no MySQL

Após executar com perfil `dev`, confira no MySQL:

```sql
USE dunamis_db;
SHOW TABLES;
SELECT * FROM roles;
SELECT * FROM church_functions;
SELECT * FROM turmas;
```

## Testar

```bash
# Testes unitários (usa H2 em memória)
mvnw.cmd test

# Health check
curl http://localhost:8080/health
```

Páginas disponíveis:

- `/` — Página inicial
- `/cadastro` — Cadastro público de membros
- `/login` — Login funcional (contacto + password)
- `/dashboard` — Área do membro (stub; Fase 5 expande)
- `/admin` — Painel admin (stub; Fase 6)
- `/siga` — Portal aluno (stub; Fase 8)
- `/health` — Status da aplicação

### Testar login (Fase 4)

1. Cadastre um membro em `/cadastro` **ou** defina variáveis de admin:
   ```
   ADMIN_CONTACTO=900000001
   ADMIN_PASSWORD=suaPasswordSegura
   ```
2. Inicie a aplicação
3. Aceda a `/login` e entre com contacto + password
4. Membro → `/dashboard` | Admin → `/admin`

## Deploy no Render (Fase 12)

A aplicação está preparada para produção com **Docker** e blueprint **`render.yaml`**.

### Pré-requisitos

1. Conta em [Render](https://render.com)
2. Repositório Git (GitHub/GitLab) com o código do projecto
3. Base de dados **MySQL 8** externa (ex.: [Railway](https://railway.app), [PlanetScale](https://planetscale.com), [Aiven](https://aiven.io))

> O Render **não inclui runtime Java nativo** — o deploy usa o `Dockerfile` incluído no projecto.

### 1. Publicar o código no GitHub

```bash
cd "Sistema Dunamis"
git init
git add .
git commit -m "Preparar deploy no Render"
git branch -M main
git remote add origin https://github.com/IlsseDevz/sistema-dunamis.git
git push -u origin main
```

### 2. Criar base MySQL

Crie uma instância MySQL e anote host, porta, base, utilizador e password.

Monte a `DB_URL` JDBC:

```
jdbc:mysql://HOST:PORTA/NOME_DB?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

### 3. Criar Web Service no Render

**Opção A — Blueprint (`render.yaml`):**

1. Render → **New** → **Blueprint**
2. Ligue o repositório GitHub
3. Preencha as variáveis sensíveis quando solicitado

**Opção B — Manual:**

1. Render → **New** → **Web Service**
2. Ligue o repositório
3. **Runtime:** Docker
4. **Health Check Path:** `/health`
5. **Plan:** Free (ou superior)

### 4. Variáveis de ambiente no Render

| Variável | Valor | Obrigatório |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | Sim |
| `DB_URL` | URL JDBC MySQL completa | Sim |
| `DB_USERNAME` | Utilizador MySQL | Sim |
| `DB_PASSWORD` | Password MySQL | Sim |
| `JPA_DDL_AUTO` | `update` (1.º deploy) | Sim |
| `ADMIN_CONTACTO` | Contacto do admin inicial | Sim (1.º deploy) |
| `ADMIN_PASSWORD` | Password segura do admin | Sim (1.º deploy) |
| `ADMIN_NAME` | Nome do admin | Opcional |
| `PORT` | *(Render define automaticamente)* | Não |

> **Importante:** Nunca coloque credenciais no código ou no repositório.

### 5. Build e arranque (Docker)

O `Dockerfile` faz build multi-stage:

- **Build:** Maven Wrapper + JDK 17 → gera o JAR
- **Runtime:** JRE 17 Alpine → executa `app.jar`

A aplicação lê a porta via `PORT` (definida em `application.yml`).

### 6. Primeiro deploy

Na primeira execução em produção:

- `JPA_DDL_AUTO=update` cria/atualiza as tabelas automaticamente
- `DataInitializer` cria roles, funções e turma padrão
- `AdminBootstrapConfig` cria o administrador se `ADMIN_CONTACTO` + `ADMIN_PASSWORD` estiverem definidos

Quando existirem migrações Flyway/Liquibase, altere para `JPA_DDL_AUTO=validate`.

### 7. Testar após deploy

- `https://SEU-SERVICO.onrender.com/health` → `{"status":"UP",...}`
- `/` — página inicial
- `/login` — autenticação
- `/cadastro` — registo de membros
- `/admin` — painel (conta admin)

### 8. Testar Docker localmente (opcional)

```bash
docker build -t sistema-dunamis .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/dunamis_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=suaPassword \
  -e JPA_DDL_AUTO=update \
  sistema-dunamis
```

### Notas

- O plano **Free** do Render pode colocar o serviço em sleep após inactividade; o primeiro pedido após sleep pode demorar ~1–2 minutos.
- Use passwords fortes para `ADMIN_PASSWORD` e `DB_PASSWORD`.
- CSRF está desactivado (formulários Thymeleaf); reactivar antes de exposição pública alargada se necessário.

## Fases de desenvolvimento

| Fase | Estado | Descrição |
|---|---|---|
| 1 | ✅ Concluída | Projeto base, configuração, estrutura |
| 2 | ✅ Concluída | Entidades JPA, repositórios, seed inicial |
| 3 | ✅ Concluída | Cadastro público de membros |
| 4 | ✅ Concluída | Login e autenticação por roles |
| 5 | ✅ Concluída | Dashboard do membro |
| 6 | ✅ Concluída | Painel administrativo e gestão de membros |
| 7 | ✅ Concluída | Escola Bíblica (turma, disciplinas, alunos) |
| 8 | ✅ Concluída | Portal SIGA do aluno |
| 9 | ✅ Concluída | Notas e presenças (admin) |
| 10 | ✅ Concluída | Relatórios PDF mensais (Escola Bíblica e igreja) |
| 11 | ✅ Concluída | Responsividade e refinamento da UI |
| 12 | ✅ Concluída | Deploy no Render (Docker + render.yaml) |

### UI responsiva (Fase 11)

- Menus laterais com **offcanvas** em ecrãs pequenos (admin, membro, SIGA)
- Sidebar fixa (`sticky`) em desktop
- Tabelas e acções adaptadas a telemóvel
- `app.css` e `app.js` centralizados (`/css/app.css`, `/js/app.js`)
- Skip link, foco visível e áreas de toque ≥ 44px

### Relatórios PDF (Fase 10)

Em `/admin/relatorios`, o administrador selecciona mês/ano e gera:

- **Escola Bíblica** — alunos activos, presenças/ausências, percentagem de presença e média das notas do período
- **Igreja** — totais de membros, situações, batizados e distribuição por função (snapshot até ao fim do mês)

Dependência: OpenPDF (`com.github.librepdf:openpdf`).

## Licença

Uso interno — Igreja Evangélica Dunamis de Jesus Cristo.
