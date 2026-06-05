# API Contracts — Exemplos JSON

Todos os exemplos usam o envelope padrão `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "opcional",
  "data": { /* payload específico do endpoint */ }
}
```

Em caso de erro:

```json
{
  "success": false,
  "message": "Descrição do erro",
  "data": null
}
```

Headers comuns:

- `Authorization: Bearer <token>` (para endpoints autenticados)
- `Content-Type: application/json` ou `multipart/form-data` quando aplicável

## 1) POST /api/users/cadastro
Request JSON:

```json
{
  "username": "joao",
  "password": "senha123",
  "nome": "João"
}
```

Successful response (201/200):

```json
{
  "success": true,
  "message": null,
  "data": {
    "mensagem": "Usuário cadastrado: joao",
    "username": "joao",
    "token": "<jwt-token>"
  }
}
```

Error (username existente):

```json
{
  "success": false,
  "message": "Username já existe: joao",
  "data": null
}
```

## 2) POST /api/users/login
Request JSON:

```json
{
  "username": "joao",
  "password": "senha123"
}
```

Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "mensagem": "Login OK! Bem-vindo, João",
    "username": "joao",
    "nome": "João",
    "token": "<jwt-token>"
  }
}
```

Unauthorized:

```json
{
  "success": false,
  "message": "Credenciais inválidas",
  "data": null
}
```

## 3) POST /api/qrcode/validar-texto
Request JSON (autenticado):

```json
{
  "codigo": "1001"
}
```

If valid:

```json
{
  "success": true,
  "message": "Checkpoint válido: Checkpoint 1 - Início ✓",
  "data": null
}
```

If not valid:

```json
{
  "success": true,
  "message": "Código \"9999\" NÃO é um checkpoint válido",
  "data": null
}
```

## 4) POST /api/qrcode/ler
Request JSON:

```json
{
  "caminho": "C:\\tmp\\qr.png"
}
```

Successful (example):

```json
{
  "success": true,
  "message": "QR lido: 1001 | Checkpoint: Checkpoint 1 - Início ✓",
  "data": null
}
```

Error reading image:

```json
{
  "success": false,
  "message": "Erro ao ler QR: arquivo não encontrado",
  "data": null
}
```

## 5) GET /api/qrcode/progresso
Response `data` payload structure:

```json
{
  "total": 4,
  "lidos": 2,
  "completo": false,
  "checkpoints": [
    { "ordem": 1, "codigo": "1001", "titulo": "Checkpoint 1 - Início", "lido": true },
    { "ordem": 2, "codigo": "2002", "titulo": "Checkpoint 2 - Meio do trajeto", "lido": true },
    { "ordem": 3, "codigo": "3003", "titulo": "Checkpoint 3 - Quase lá", "lido": false },
    { "ordem": 4, "codigo": "4004", "titulo": "Checkpoint 4 - Final", "lido": false }
  ]
}
```

Envelope example:

```json
{
  "success": true,
  "message": null,
  "data": { /* payload acima */ }
}
```

## 6) GET /api/album (autenticado)
Response `data` payload example:

```json
{
  "id": 123,
  "username": "joao",
  "routeComplete": false,
  "quantidadeFotos": 1,
  "podeAdicionar": true,
  "fotos": [
    { "id": 10, "originalName": "foto.png", "url": "/uploads/abcd.png", "uploadedAt": "2026-06-05T12:00:00" }
  ]
}
```

Envelope:

```json
{
  "success": true,
  "message": null,
  "data": { /* payload acima */ }
}
```

## 7) POST /api/album/upload
Request: multipart/form-data field `file` (Authorization header required)

Success `data` example:

```json
{
  "mensagem": "Foto enviada com sucesso",
  "id": 11,
  "fileName": "9d7ee856-da29-4917-9a93-aeb3c28f1068.png",
  "url": "/uploads/9d7ee856-da29-4917-9a93-aeb3c28f1068.png"
}
```

Envelope:

```json
{
  "success": true,
  "message": null,
  "data": { /* payload acima */ }
}
```

## 8) POST /api/album/moldura/{fotoId}
Request JSON:

```json
{
  "frame": 1
}
```

Success example:

```json
{
  "success": true,
  "message": null,
  "data": {
    "mensagem": "Moldura aplicada com sucesso!",
    "frame": 1,
    "arquivo": "abcd-framed.png",
    "url": "/uploads/abcd-framed.png"
  }
}
```

Error if route not complete:

```json
{
  "success": false,
  "message": "Complete todos os 4 checkpoints primeiro!",
  "data": null
}
```

---

Coloquei este arquivo como referência para o frontend — se quiser, eu gero também um arquivo Postman collection ou um `openapi.json` enriquecido com exemplos automaticamente. Diga qual prefere. 
