# Documentacao do Projeto XaxinQRCode

## Visao geral

O projeto `XaxinProjeto_2_0` e uma aplicacao Java com Spring Boot para cadastro e login de usuarios, autenticacao via JWT, validacao de QR Codes que representam checkpoints de um trajeto, album de fotos (ate 10 por usuario) e aplicacao de molduras sobre as fotos apos completar o trajeto.

A aplicacao expoe uma API REST protegida. O usuario se cadastra ou faz login, recebe um token JWT e usa esse token para acessar os demais endpoints.

Os QR Codes validos atualmente representam quatro checkpoints fixos:

| Ordem | Codigo | Titulo |
|---|---|---|
| 1 | `1001` | Checkpoint 1 - Inicio |
| 2 | `2002` | Checkpoint 2 - Meio do trajeto |
| 3 | `3003` | Checkpoint 3 - Quase la |
| 4 | `4004` | Checkpoint 4 - Final |

Esses checkpoints sao criados automaticamente na inicializacao da aplicacao pela classe `DataInitializer`, caso a tabela `checkpoints` esteja vazia.

## Tecnologias utilizadas

| Tecnologia | Uso no projeto |
|---|---|
| Java 17 | Linguagem principal |
| Spring Boot 3.2.4 | Base da aplicacao |
| Spring Web | Criacao dos endpoints REST |
| Spring Data JPA | Persistencia com banco de dados |
| Spring Security | Protecao dos endpoints |
| JWT com JJWT | Geracao e validacao de tokens |
| BCrypt | Criptografia de senhas |
| PostgreSQL | Banco principal configurado |
| H2 Database | Banco em memoria para testes |
| ZXing | Leitura de QR Code a partir de imagem |
| AWT/ImageIO | Processamento de imagens para molduras |
| Maven | Build e gerenciamento de dependencias |

## Estrutura do projeto

```text
XaxinProjeto_2_0/
├── pom.xml
├── mvnw.cmd
├── TESTES.md
├── DOCUMENTACAO.md
├── src/
│   └── main/
│       ├── java/
│       │   └── com/xaxin/qrcode/
│       │       ├── XaxinApplication.java
│       │       ├── LeitorQRCode.java
│       │       ├── config/
│       │       │   └── DataInitializer.java
│       │       ├── controller/
│       │       │   ├── AlbumController.java
│       │       │   ├── QrCodeController.java
│       │       │   └── UserController.java
│       │       ├── frame/
│       │       │   └── FrameService.java
│       │       ├── model/
│       │       │   ├── Album.java
│       │       │   ├── Checkpoint.java
│       │       │   ├── Foto.java
│       │       │   ├── QrNumber.java
│       │       │   └── User.java
│       │       ├── repository/
│       │       │   ├── AlbumRepository.java
│       │       │   ├── CheckpointRepository.java
│       │       │   ├── FotoRepository.java
│       │       │   ├── QrNumberRepository.java
│       │       │   └── UserRepository.java
│       │       ├── security/
│       │       │   ├── JwtAuthenticationFilter.java
│       │       │   ├── JwtTokenProvider.java
│       │       │   └── SecurityConfig.java
│       │       └── service/
│       │           ├── AlbumService.java
│       │           ├── QrCodeService.java
│       │           ├── TrajetoService.java
│       │           └── UserService.java
│       └── resources/
│           ├── application.properties
│           ├── application-h2.properties
│           └── static/
│               ├── teste.html
│               ├── frames/
│               │   ├── MolduraCaracol.png
│               │   ├── MolduraCaracol2.png
│               │   └── MolduraCaracol3.png
│               └── uploads/
└── target/
```

## Camadas da aplicacao

### Entrada da aplicacao

`XaxinApplication.java` e a classe principal. Ela inicia o Spring Boot com:

```java
SpringApplication.run(XaxinApplication.class, args);
```

### Controllers

Os controllers recebem as requisicoes HTTP e chamam os servicos.

| Controller | Rota base | Responsabilidade |
|---|---|---|
| `UserController` | `/api/users` | Cadastro e login de usuarios |
| `QrCodeController` | `/api/qrcode` | Leitura, validacao e progresso de QR Codes |
| `AlbumController` | `/api/album` | Gerenciamento do album, upload de fotos e aplicacao de molduras |

### Services

Os services concentram as regras de negocio.

| Service | Responsabilidade |
|---|---|
| `UserService` | Cadastrar usuarios, criptografar senhas e validar login |
| `QrCodeService` | Ler QR Code de uma imagem usando ZXing e salvar numeros lidos |
| `TrajetoService` | Validar checkpoints, registrar leituras e calcular progresso |
| `AlbumService` | Gerenciar album do usuario, upload de fotos (max 10) e aplicar molduras |
| `FrameService` | Sobrepor imagem de moldura PNG sobre a foto escolhida |

### Repositories

Os repositories fazem acesso ao banco via Spring Data JPA.

| Repository | Entidade | Metodos principais |
|---|---|---|
| `UserRepository` | `User` | `findByUsername` |
| `QrNumberRepository` | `QrNumber` | CRUD padrao do JPA |
| `CheckpointRepository` | `Checkpoint` | `findAllByOrderByOrdemAsc`, `findByCodigo` |
| `AlbumRepository` | `Album` | `findByUser`, `findByUserId` |
| `FotoRepository` | `Foto` | `findByAlbumId` |

### Models

As entidades representam as tabelas do banco.

| Entidade | Tabela | Descricao |
|---|---|---|
| `User` | `app_users` | Usuario do sistema |
| `QrNumber` | `qr_numbers` | Numero lido de QR Code |
| `Checkpoint` | `checkpoints` | Checkpoint valido do trajeto |
| `Album` | `albums` | Album vinculado a um usuario (1 por usuario) |
| `Foto` | `fotos` | Foto pertencente a um album (ate 10 por album) |

## Fluxo completo do projeto

```
1. INICIO
   │
   ├── (novo usuario) → POST /api/users/cadastro → retorna token JWT
   │                    Body: { "username", "password", "nome" }
   │
   └── (ja cadastrado) → POST /api/users/login → retorna token JWT
                         Body: { "username", "password" }

2. VERIFICAR O ALBUM (opcional)
   │
   └── GET /api/album → retorna dados do album, fotos, rota completa?
       Header: Authorization: Bearer <token>

3. UPLOAD DE FOTOS (ate 10)
   │
   └── POST /api/album/upload → salva foto no servidor
       Header: Authorization: Bearer <token>
       Body: multipart/form-data (campo "file")
       Limite: maximo 10 fotos por album

4. LER OS 4 QR CODES (checkpoints do trajeto)
   │
   ├── POST /api/qrcode/validar-texto → valida codigo manual
   │   Header: Authorization: Bearer <token>
   │   Body: { "codigo": "1001" }  (ou 2002, 3003, 4004)
   │
   └── POST /api/qrcode/ler → le QR de imagem
       Header: Authorization: Bearer <token>
       Body: { "caminho": "C:\\qr.png" }

5. CONSULTAR PROGRESSO
   │
   └── GET /api/qrcode/progresso → mostra quais checkpoints ja foram lidos
       Header: Authorization: Bearer <token>
       Quando "completo": true, a rota e liberada

6. APLICAR MOLDURA (so liberado apos completar os 4 checkpoints)
   │
   └── POST /api/album/moldura/{fotoId} → sobrepoe moldura PNG na foto
       Header: Authorization: Bearer <token>
       Body: { "frame": 1 }  (1, 2 ou 3)
       Retorna: url da foto com moldura gerada

7. VISUALIZAR RESULTADO
   │
   └── Acessar a URL retornada para ver a foto com moldura
```

## Como funciona a aplicacao de molduras

## Preparacao do backend antes do frontend

Antes de iniciar o desenvolvimento do frontend, garanta que o backend esteja pronto e acessivel. Passos principais:

- Variaveis de ambiente essenciais:
  - `JWT_SECRET` — secreto do JWT (NUNCA comite este valor no repo). Ex: `export JWT_SECRET=uma_chave_segura`.
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` para conexao com PostgreSQL em producao.

- Documentacao / OpenAPI:
  - A API exposta pelo backend fica documentada automaticamente em runtime via Springdoc.
  - URL do Swagger UI (dev): `/swagger-ui/index.html`
  - URL do JSON OpenAPI: `/v3/api-docs`

- Migrações (Flyway):
  - O projeto usa Flyway. Migractions estao em `src/main/resources/db/migration`.
  - Em CI/produçao, execute as migrações antes de iniciar a aplicacao (ou deixe a aplicacao aplicar automaticamente). Exemplo local:
    ```bash
    mvn -DskipTests flyway:migrate
    # ou
    mvn -DskipTests spring-boot:run -Dspring-boot.run.profiles=h2
    ```

- Actuator (health):
  - Endpoints de health estao expostos: `/actuator/health` e `/actuator/info` (configuraveis em properties).

- Uploads e armazenamento:
  - Em dev a estrategia padrao e `app.upload.strategy=local` (pasta `uploads/`).
  - Em producao considere usar S3 ou storage persistente — as propriedades de S3 estao como placeholders (`aws.s3.*`).

- Git LFS / artefatos grandes:
  - O repositório foi limpo para remover o JAR de `target/` e configurado para rastrear `*.jar` via Git LFS.
  - Colaboradores devem re-clonar o repo ou executar `git lfs install` e atualizar seus clones.

## Como rodar localmente (resumo rapido)

1. Exportar segredo JWT e configurar DB (para H2 não precisa):
```bash
export JWT_SECRET=uma_chave_segura
# Opcional para PostgreSQL
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/xaxindb
export SPRING_DATASOURCE_USERNAME=usuario
export SPRING_DATASOURCE_PASSWORD=senha
```

2. Rodar migrações (opcional, a aplicação faz isso automaticamente se `spring.flyway.enabled=true`):
```bash
mvn -DskipTests flyway:migrate
```

3. Build e run:
```bash
mvn -DskipTests package
java -jar target/xaxin-projeto-qrcode-1.0.0.jar --spring.profiles.active=h2
```

4. Abrir Swagger UI: http://localhost:8080/swagger-ui/index.html


### FrameService

A classe `FrameService` (pacote `frame`) e responsavel por sobrepor uma das tres molduras PNG sobre a foto escolhida pelo usuario.

Processo:

1. Valida o indice da moldura (1, 2 ou 3).
2. Carrega a foto original do disco.
3. Carrega a moldura PNG correspondente do classpath (`static/frames/`).
4. Redimensiona a moldura para o tamanho exato da foto.
5. Desenha a foto no fundo e a moldura por cima (com transparencia).
6. Salva a imagem resultante como `{fotoOriginal}_frame{indice}.png` na mesma pasta.
7. Retorna o caminho do arquivo gerado.

As tres molduras disponiveis:

| Indice | Arquivo |
|---|---|
| 1 | `MolduraCaracol.png` |
| 2 | `MolduraCaracol2.png` |
| 3 | `MolduraCaracol3.png` |

### Regras de negocio

- A moldura **so pode ser aplicada** se o usuario tiver lido todos os 4 checkpoints (`routeComplete == true`).
- O usuario pode escolher entre 3 molduras diferentes.
- A foto precisa pertencer ao album do usuario logado.
- A imagem gerada e salva no mesmo diretorio de uploads.

## Configuracao

### PostgreSQL

O arquivo `src/main/resources/application.properties` configura o PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sua_base
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

server.port=8080

jwt.secret=XaxinProject2026SecretKeySuperSeguraParaQRCode1234567890
jwt.expiration=172800000
```

Antes de rodar com PostgreSQL, ajuste:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### H2 para testes

O arquivo `src/main/resources/application-h2.properties` permite rodar a aplicacao com banco em memoria:

```properties
spring.datasource.url=jdbc:h2:mem:xaxindb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

server.port=8080

jwt.secret=XaxinProject2026SecretKeySuperSeguraParaQRCode1234567890
jwt.expiration=172800000
```

Com o profile H2 ativo, o console fica em:

```text
http://localhost:8080/h2-console
```

## Como compilar

No PowerShell:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Se o wrapper local nao estiver disponivel, use o Maven pela maquina:

No PowerShell:
```powershell
& "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd" clean package -DskipTests
```

No CMD:
```cmd
"%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd" clean package -DskipTests
```

O build gera o arquivo:

```text
target/xaxin-projeto-qrcode-1.0.0.jar
```

## Como executar

### Executar com PostgreSQL

Depois de configurar `application.properties`:

```powershell
java -jar target/xaxin-projeto-qrcode-1.0.0.jar
```

### Executar com H2

Para testar sem PostgreSQL:

```powershell
java -jar target/xaxin-projeto-qrcode-1.0.0.jar --spring.profiles.active=h2
```

A aplicacao sobe em:

```text
http://localhost:8080
```

## Interface de teste

O projeto possui uma pagina estatica para testar a API:

```text
src/main/resources/static/teste.html
```

Com o servidor rodando, acesse:

```text
http://localhost:8080/teste.html
```

Essa pagina permite:

- verificar se o servidor esta online;
- cadastrar usuario;
- fazer login;
- visualizar o token JWT;
- validar codigo QR manualmente;
- consultar progresso com grid visual dos checkpoints;
- testar leitura de QR Code a partir de caminho de imagem.

## Autenticacao e seguranca

A aplicacao usa JWT com Spring Security.

### Endpoints publicos

Estes endpoints nao precisam de token:

| Metodo | Endpoint |
|---|---|
| `POST` | `/api/users/cadastro` |
| `POST` | `/api/users/login` |
| `GET` | `/teste.html` |
| `GET` | `/static/**` |
| `GET` | `/webjars/**` |
| `GET` | `/uploads/**` |
| `GET` | `/frames/**` |
| `GET` | `/h2-console/**` |

### Endpoints protegidos

Qualquer outro endpoint exige header:

```http
Authorization: Bearer SEU_TOKEN_AQUI
```

### Fluxo de autenticacao

1. Usuario chama `/api/users/cadastro` ou `/api/users/login`.
2. A API valida os dados.
3. A API gera um token JWT usando `JwtTokenProvider`.
4. O cliente envia esse token nas proximas requisicoes.
5. `JwtAuthenticationFilter` extrai e valida o token.
6. Se o token for valido, a requisicao segue autenticada.

### Senhas

As senhas sao armazenadas criptografadas com BCrypt. A classe `UserService` usa `PasswordEncoder` para:

- criptografar a senha no cadastro;
- comparar a senha enviada no login com a senha criptografada no banco.

### Expiracao do token

O valor atual e:

```properties
jwt.expiration=172800000
```

Isso equivale a 2 dias em milissegundos.

## API REST

### Cadastro de usuario

Cria um novo usuario e retorna um token JWT.

```http
POST /api/users/cadastro
Content-Type: application/json
```

Body:

```json
{
  "username": "joao",
  "password": "123",
  "nome": "Joao"
}
```

Resposta de sucesso:

```json
{
  "mensagem": "Usuario cadastrado: joao",
  "username": "joao",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## Notas importantes antes do desenvolvimento do Front-end

Antes de começar a implementar o front-end, certifique-se destas configurações no backend:

- `JWT_SECRET` (variável de ambiente): **obrigatória**. A aplicação não inicia se não existir ou se for menor que 32 caracteres.
- `FRONTEND_ORIGINS` (variável de ambiente opcional): lista de origens CORS permitidas, separadas por vírgula. Exemplo: `http://localhost:3000,https://app.seudominio.com`. Se não definida, usa `http://localhost:3000` por padrão.

Exemplo de execução local (PowerShell):

```powershell
$env:JWT_SECRET = "uma-chave-muito-segura-com-32-ou-mais-caracteres"
$env:FRONTEND_ORIGINS = "http://localhost:3000"
mvn -DskipTests spring-boot:run
```

Migração de dados para `qr_numbers.user_id`:

- Foi adicionada uma migração placeholder em `src/main/resources/db/migration/V3__populate_qr_user.sql`.
- A migração automática requer uma regra de mapeamento entre registros legados e usuários (ex.: assoc. por algum campo auxiliar). Se não houver regra, deixe `user_id` nulo e execute a migração manualmente quando houver política de mapeamento.

Se quiser, eu posso:
- adicionar um script Java/SQL para tentar popular `user_id` com uma heurística (forneça a regra), ou
- manter a migração como manual e documentar os passos.


Possiveis erros:

| Status | Motivo |
|---|---|
| `400` | Faltam campos obrigatorios |
| `400` | Username ja existe |

### Login

Valida credenciais e retorna um token JWT.

```http
POST /api/users/login
Content-Type: application/json
```

Body:

```json
{
  "username": "joao",
  "password": "123"
}
```

Resposta de sucesso:

```json
{
  "mensagem": "Login OK! Bem-vindo, Joao",
  "username": "joao",
  "nome": "Joao",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Possiveis erros:

| Status | Motivo |
|---|---|
| `400` | Faltam campos obrigatorios |
| `401` | Credenciais invalidas |

### Validar QR Code por texto

Valida manualmente um codigo e registra a leitura caso seja um checkpoint valido.

```http
POST /api/qrcode/validar-texto
Content-Type: application/json
Authorization: Bearer SEU_TOKEN_AQUI
```

Body:

```json
{
  "codigo": "1001"
}
```

Resposta para codigo valido:

```text
Checkpoint valido: Checkpoint 1 - Inicio
```

Resposta para codigo invalido:

```text
Codigo "9999" NAO e um checkpoint valido
```

Possiveis erros:

| Status | Motivo |
|---|---|
| `400` | Campo `codigo` ausente |
| `403` | Token ausente ou invalido |

### Ler QR Code de imagem

Le um QR Code de um arquivo de imagem no disco do servidor, extrai um numero e valida como checkpoint.

```http
POST /api/qrcode/ler
Content-Type: application/json
Authorization: Bearer SEU_TOKEN_AQUI
```

Body:

```json
{
  "caminho": "C:\\qr1001.png"
}
```

Resposta para QR valido:

```text
QR lido: 1001 | Checkpoint: Checkpoint 1 - Inicio
```

Resposta para QR invalido:

```text
QR lido: 9999 (NAO e um checkpoint valido)
```

Possiveis erros:

| Status | Motivo |
|---|---|
| `400` | Campo `caminho` ausente |
| `400` | Arquivo inexistente |
| `400` | QR Code nao encontrado na imagem |
| `400` | QR Code nao contem um numero inteiro valido |
| `403` | Token ausente ou invalido |

Importante: o caminho enviado precisa existir no computador onde o servidor Java esta rodando.

### Consultar progresso

Retorna o progresso dos quatro checkpoints.

```http
GET /api/qrcode/progresso
Authorization: Bearer SEU_TOKEN_AQUI
```

Resposta:

```json
{
  "total": 4,
  "lidos": 1,
  "completo": false,
  "checkpoints": [
    {
      "ordem": 1,
      "codigo": "1001",
      "titulo": "Checkpoint 1 - Inicio",
      "lido": true
    },
    {
      "ordem": 2,
      "codigo": "2002",
      "titulo": "Checkpoint 2 - Meio do trajeto",
      "lido": false
    }
  ]
}
```

Possiveis erros:

| Status | Motivo |
|---|---|
| `403` | Token ausente ou invalido |

Quando `"completo": true`, significa que os 4 checkpoints foram lidos e a rota esta liberada para aplicar molduras.

### Obter album do usuario

Retorna os dados do album do usuario logado, incluindo fotos.

```http
GET /api/album
Authorization: Bearer SEU_TOKEN_AQUI
```

Resposta:

```json
{
  "id": 1,
  "username": "joao",
  "routeComplete": false,
  "quantidadeFotos": 2,
  "podeAdicionar": true,
  "fotos": [
    {
      "id": 1,
      "originalName": "foto.jpg",
      "url": "/uploads/abc123.jpg",
      "uploadedAt": "2026-05-23T12:00:00"
    }
  ]
}
```

Possiveis erros:

| Status | Motivo |
|---|---|
| `401` | Usuario nao encontrado |
| `403` | Token ausente ou invalido |

### Upload de foto

Faz upload de uma foto para o album do usuario (maximo 10).

```http
POST /api/album/upload
Authorization: Bearer SEU_TOKEN_AQUI
Content-Type: multipart/form-data

file: (arquivo de imagem)
```

Resposta de sucesso:

```json
{
  "mensagem": "Foto enviada com sucesso",
  "id": 1,
  "fileName": "uuid-arquivo.jpg",
  "url": "/uploads/uuid-arquivo.jpg"
}
```

Possiveis erros:

| Status | Motivo |
|---|---|
| `400` | Arquivo vazio |
| `400` | Album ja possui 10 fotos |
| `401` | Usuario nao encontrado |
| `403` | Token ausente ou invalido |

### Aplicar moldura na foto

Aplica uma das tres molduras sobre a foto escolhida. So funciona se todos os 4 checkpoints tiverem sido lidos.

```http
POST /api/album/moldura/{fotoId}
Authorization: Bearer SEU_TOKEN_AQUI
Content-Type: application/json
```

Body:

```json
{
  "frame": 1
}
```

O campo `frame` aceita os valores 1, 2 ou 3.

Resposta de sucesso:

```json
{
  "mensagem": "Moldura aplicada com sucesso!",
  "frame": 1,
  "arquivo": "foto_frame1.png",
  "url": "/uploads/foto_frame1.png"
}
```

Possiveis erros:

| Status | Motivo |
|---|---|
| `400` | Campo `frame` ausente ou invalido |
| `400` | Rota nao completada (faltam checkpoints) |
| `400` | Foto nao encontrada no album |
| `401` | Usuario nao encontrado |
| `403` | Token ausente ou invalido |

## Persistencia e tabelas

Com `spring.jpa.hibernate.ddl-auto=update`, o Hibernate cria ou atualiza as tabelas automaticamente conforme as entidades.

### `app_users`

Armazena usuarios.

| Campo | Tipo aproximado | Descricao |
|---|---|---|
| `id` | Long | Identificador |
| `username` | String | Login do usuario |
| `password` | String | Senha criptografada |
| `nome` | String | Nome exibido |

### `checkpoints`

Armazena os checkpoints validos.

| Campo | Tipo aproximado | Descricao |
|---|---|---|
| `id` | Long | Identificador |
| `ordem` | Integer | Ordem no trajeto |
| `codigo` | String | Codigo do QR Code |
| `titulo` | String | Nome do checkpoint |

### `qr_numbers`

Armazena as leituras de QR Code.

| Campo | Tipo aproximado | Descricao |
|---|---|---|
| `id` | Long | Identificador |
| `numero` | Integer | Numero lido |

### `albums`

Armazena album vinculado a um usuario (1 por usuario).

| Campo | Tipo aproximado | Descricao |
|---|---|---|
| `id` | Long | Identificador |
| `user_id` | Long | Usuario dono do album (unique) |
| `route_complete` | Boolean | Indica se o trajeto foi concluido |

### `fotos`

Armazena fotos pertencentes a um album (ate 10 por album).

| Campo | Tipo aproximado | Descricao |
|---|---|---|
| `id` | Long | Identificador |
| `file_name` | String | Nome salvo do arquivo |
| `original_name` | String | Nome original enviado |
| `url` | String | URL de acesso |
| `uploaded_at` | LocalDateTime | Data/hora de upload |
| `album_id` | Long | Album relacionado |

## Dados iniciais

A classe `DataInitializer` roda ao iniciar a aplicacao e cria os quatro checkpoints padrao se ainda nao existir nenhum checkpoint no banco:

```text
1001 - Checkpoint 1 - Inicio
2002 - Checkpoint 2 - Meio do trajeto
3003 - Checkpoint 3 - Quase la
4004 - Checkpoint 4 - Final
```

Se ja houver pelo menos um registro na tabela `checkpoints`, o inicializador nao cria nada.

## Classe utilitaria `LeitorQRCode`

`LeitorQRCode.java` e uma classe independente, fora do fluxo REST principal.

Ela:

- le uma imagem de QR Code;
- extrai um numero inteiro;
- conecta diretamente ao PostgreSQL via JDBC;
- cria a tabela `qr_numbers`, se necessario;
- salva o numero lido.

Essa classe possui configuracoes fixas de banco:

```java
private static final String DB_URL = "jdbc:postgresql://localhost:5432/sua_base";
private static final String DB_USER = "seu_usuario";
private static final String DB_PASSWORD = "sua_senha";
```

Para usar essa classe diretamente, e necessario ajustar esses valores ou preferir o endpoint `/api/qrcode/ler`, que usa a configuracao do Spring.

## Assets estaticos

Arquivos em `src/main/resources/static` sao servidos pelo Spring Boot.

| Arquivo | Descricao |
|---|---|
| `teste.html` | Interface web de teste da API |
| `frames/MolduraCaracol.png` | Moldura 1 |
| `frames/MolduraCaracol2.png` | Moldura 2 |
| `frames/MolduraCaracol3.png` | Moldura 3 |

O diretorio `uploads/` e criado automaticamente e armazena as fotos enviadas pelos usuarios.

## Comandos uteis

### Build

```powershell
.\mvnw.cmd clean package -DskipTests
```

### Rodar com H2

```powershell
java -jar target/xaxin-projeto-qrcode-1.0.0.jar --spring.profiles.active=h2
```

### Rodar com PostgreSQL

```powershell
java -jar target/xaxin-projeto-qrcode-1.0.0.jar
```

### Cadastro via PowerShell

```powershell
curl.exe --url "http://localhost:8080/api/users/cadastro" -X POST -H "Content-Type: application/json" -d "{\"username\":\"joao\",\"password\":\"123\",\"nome\":\"Joao\"}"
```

### Login via PowerShell

```powershell
curl.exe --url "http://localhost:8080/api/users/login" -X POST -H "Content-Type: application/json" -d "{\"username\":\"joao\",\"password\":\"123\"}"
```

### Ver progresso via PowerShell

```powershell
curl.exe --url "http://localhost:8080/api/qrcode/progresso" -X GET -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

### Validar codigo manual via PowerShell

```powershell
curl.exe --url "http://localhost:8080/api/qrcode/validar-texto" -X POST -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN_AQUI" -d "{\"codigo\":\"1001\"}"
```

### Upload de foto via PowerShell

```powershell
curl.exe --url "http://localhost:8080/api/album/upload" -X POST -H "Authorization: Bearer SEU_TOKEN_AQUI" -F "file=@C:\caminho\foto.jpg"
```

### Aplicar moldura via PowerShell

```powershell
curl.exe --url "http://localhost:8080/api/album/moldura/1" -X POST -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN_AQUI" -d "{\"frame\":1}"
```

### Ver album via PowerShell

```powershell
curl.exe --url "http://localhost:8080/api/album" -X GET -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

## Observacoes importantes

- O progresso atual e calculado com base em todos os registros da tabela `qr_numbers`.
- Nesta versao, o progresso nao e separado por usuario. Se um usuario ler o checkpoint `1001`, esse numero fica registrado globalmente.
- A tabela `qr_numbers` pode ter leituras repetidas. O progresso considera se um codigo ja apareceu ao menos uma vez.
- Os endpoints protegidos retornam erro quando nao recebem token JWT valido.
- O segredo JWT esta configurado em arquivo de propriedades. Em producao, o ideal e usar variaveis de ambiente ou outro mecanismo seguro.
- Cada usuario possui 1 album (criado automaticamente no primeiro acesso).
- O album permite ate 10 fotos.
- A aplicacao de moldura so e liberada apos a leitura de todos os 4 checkpoints.
- Sao 3 molduras disponiveis: `MolduraCaracol.png`, `MolduraCaracol2.png` e `MolduraCaracol3.png`.
- Ao aplicar a moldura, o FrameService redimensiona a moldura para o tamanho exato da foto e sobrepoe com transparencia.
- A foto com moldura e salva como `{nomeOriginal}_frame{indice}.png` na pasta `uploads/`.

## Guia de testes

O arquivo `TESTES.md` contem um roteiro pratico com comandos `curl` para testar:

- cadastro;
- login;
- login invalido;
- acesso sem token;
- consulta de progresso;
- validacao de QR valido;
- validacao de QR invalido;
- progresso completo;
- leitura de QR por imagem.

Use `TESTES.md` como checklist operacional e esta documentacao como referencia geral do projeto.