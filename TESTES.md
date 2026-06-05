# Guia de Testes - XaxinProjeto_2_0

## Pré-requisitos
- PostgreSQL rodando e configurado no `application.properties`
- Projeto compilado

## ⚠️ Importante
Se o servidor Java estiver rodando, você **não** consegue compilar (o JAR fica bloqueado). Feche o servidor com `Ctrl + C` no terminal dele antes de compilar.

## Como compilar o projeto

**No CMD (cmd.exe):**
```cmd
"%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd" clean package -DskipTests
```

**No PowerShell:**
```powershell
& "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd" clean package -DskipTests
```

## Como iniciar o servidor (com H2 - sem precisar de PostgreSQL)

**No CMD:**
```cmd
java -jar target/xaxin-projeto-qrcode-1.0.0.jar --spring.profiles.active=h2
```

**No PowerShell:**
```powershell
java -jar target/xaxin-projeto-qrcode-1.0.0.jar --spring.profiles.active=h2
```

O servidor sobe em `http://localhost:8080`

## ⚡ Comandos para PowerShell (atenção: use `--url`)

No PowerShell, o `curl.exe` pode rejeitar a URL com `:` na porta. **Sempre use `--url` com aspas duplas:**

```powershell
curl.exe --url "http://localhost:8080/api/users/cadastro" -X POST -H "Content-Type: application/json" -d "{\"username\":\"joao\",\"password\":\"123\",\"nome\":\"Joao\"}"
```

---

## TESTE 1 - Cadastro de usuário (público)

```bash
curl -X POST http://localhost:8080/api/users/cadastro ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"joao\",\"password\":\"123\",\"nome\":\"João Silva\"}"
```

**Esperado:** Status 200, retorna token JWT
```json
{
  "mensagem": "Usuário cadastrado: joao",
  "username": "joao",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## TESTE 2 - Login (público)

```bash
curl -X POST http://localhost:8080/api/users/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"joao\",\"password\":\"123\"}"
```

**Esperado:** Status 200, retorna token JWT
```json
{
  "mensagem": "Login OK! Bem-vindo, João Silva",
  "username": "joao",
  "nome": "João Silva",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

> 💡 **Copie o token retornado** (valor do campo "token") para usar nos testes seguintes.

---

## TESTE 3 - Login com senha errada

```bash
curl -X POST http://localhost:8080/api/users/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"joao\",\"password\":\"senha_errada\"}"
```

**Esperado:** Status 401
```json
"Credenciais inválidas"
```

---

## TESTE 4 - Acessar API sem token (deve negar)

```bash
curl -X GET http://localhost:8080/api/qrcode/progresso
```

**Esperado:** Status 403 - Forbidden (acesso negado)

---

## TESTE 5 - Verificar progresso (com token)

> Substitua `SEU_TOKEN_AQUI` pelo token obtido no login

```bash
curl -X GET http://localhost:8080/api/qrcode/progresso ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Esperado:** Status 200, progresso vazio
```json
{
  "total": 4,
  "lidos": 0,
  "completo": false,
  "checkpoints": [
    {"ordem": 1, "codigo": "1001", "titulo": "Checkpoint 1 - Início", "lido": false},
    {"ordem": 2, "codigo": "2002", "titulo": "Checkpoint 2 - Meio do trajeto", "lido": false},
    {"ordem": 3, "codigo": "3003", "titulo": "Checkpoint 3 - Quase lá", "lido": false},
    {"ordem": 4, "codigo": "4004", "titulo": "Checkpoint 4 - Final", "lido": false}
  ]
}
```

---

## TESTE 6 - Validar QR code manualmente (Checkpoint 1)

```bash
curl -X POST http://localhost:8080/api/qrcode/validar-texto ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI" ^
  -d "{\"codigo\":\"1001\"}"
```

**Esperado:** Status 200
```json
"Checkpoint válido: Checkpoint 1 - Início ✓"
```

---

## TESTE 7 - Validar QR code inválido

```bash
curl -X POST http://localhost:8080/api/qrcode/validar-texto ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI" ^
  -d "{\"codigo\":\"9999\"}"
```

**Esperado:** Status 200
```json
"Código "9999" NÃO é um checkpoint válido"
```

---

## TESTE 8 - Verificar progresso após ler 1 checkpoint

```bash
curl -X GET http://localhost:8080/api/qrcode/progresso ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Esperado:** Agora mostra `"lido": true` para o checkpoint 1001 e `"lidos": 1`

---

## TESTE 9 - Validar os 4 checkpoints restantes

```bash
curl -X POST http://localhost:8080/api/qrcode/validar-texto ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI" ^
  -d "{\"codigo\":\"2002\"}"
```

```bash
curl -X POST http://localhost:8080/api/qrcode/validar-texto ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI" ^
  -d "{\"codigo\":\"3003\"}"
```

```bash
curl -X POST http://localhost:8080/api/qrcode/validar-texto ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI" ^
  -d "{\"codigo\":\"4004\"}"
```

---

## TESTE 10 - Progresso completo (todos os 4 checkpoints lidos)

```bash
curl -X GET http://localhost:8080/api/qrcode/progresso ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Esperado:**
```json
{
  "total": 4,
  "lidos": 4,
  "completo": true,
  "checkpoints": [
    {"ordem": 1, "codigo": "1001", "titulo": "Checkpoint 1 - Início", "lido": true},
    {"ordem": 2, "codigo": "2002", "titulo": "Checkpoint 2 - Meio do trajeto", "lido": true},
    {"ordem": 3, "codigo": "3003", "titulo": "Checkpoint 3 - Quase lá", "lido": true},
    {"ordem": 4, "codigo": "4004", "titulo": "Checkpoint 4 - Final", "lido": true}
  ]
}
```

---

## TESTE 11 - Ler QR code de imagem (com token)

> ⚠️ Você precisa ter uma imagem de QR code salva em disco, ex: `C:\qr1001.png` com o conteúdo "1001"

```bash
curl -X POST http://localhost:8080/api/qrcode/ler ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI" ^
  -d "{\"caminho\":\"C:\\qr1001.png\"}"
```

**Esperado (QR válido):**
```json
"QR lido: 1001 | Checkpoint: Checkpoint 1 - Início ✓"
```

**Esperado (QR inválido):**
```json
"QR lido: 9999 (NÃO é um checkpoint válido)"
```

---

## Resumo dos cenários testados

| # | Teste | Status esperado |
|---|-------|----------------|
| 1 | Cadastro de usuário | ✅ 200 + token |
| 2 | Login com credenciais corretas | ✅ 200 + token |
| 3 | Login com senha errada | ✅ 401 |
| 4 | API sem token | ✅ 403 |
| 5 | Ver progresso vazio | ✅ 200 |
| 6 | Validar QR 1001 | ✅ Checkpoint 1 |
| 7 | Validar QR inválido 9999 | ✅ Não é válido |
| 8 | Progresso após 1 leitura | ✅ lidos=1 |
| 9 | Validar QRs 2002, 3003, 4004 | ✅ Todos válidos |
| 10 | Progresso completo | ✅ lidos=4, completo=true |
| 11 | Ler QR de imagem | ✅ Conforme o caso |