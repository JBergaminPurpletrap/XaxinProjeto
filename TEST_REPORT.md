# E2E automation: flow and expected results

This repository includes two reusable scripts for end-to-end smoke tests of the backend:

- `scripts/e2e-test.ps1` — PowerShell script (Windows).
- `scripts/e2e-test.sh` — Bash script (Linux / macOS).

What the scripts do (sequence):

1. Start the Spring Boot jar (optional) with the H2 profile.
2. Register a test user (default: `tester` / `123`).
3. Login and obtain JWT token.
4. Validate the four checkpoint codes: `1001`, `2002`, `3003`, `4004`.
5. Query `GET /api/qrcode/progresso` to verify `completo: true`.
6. Upload a small test image to `/api/album/upload`.
7. Apply frame `1` to the uploaded photo (`POST /api/album/moldura/{fotoId}`).
8. Query album and write a markdown report (PowerShell script writes `target/e2e-report.md`).

How to run (Windows PowerShell):

```powershell
# from project root
.
cd scripts
pwsh .\e2e-test.ps1    # defaults: starts jar, uses H2 profile
```

Or without starting the jar (if app already running):

```powershell
pwsh .\e2e-test.ps1 -NoStart
```

How to run (Linux / macOS):

```bash
cd scripts
./e2e-test.sh
```

Notes and troubleshooting
- Scripts expect `curl` installed. Bash script also requires `jq` for JSON pretty-printing.
- PowerShell script uses `Invoke-RestMethod` and `curl.exe` for multipart upload; run from PowerShell 7+ if possible.
- The scripts default to `target/xaxin-projeto-qrcode-1.0.0.jar` — build first with `mvn clean package -DskipTests`.
- The PowerShell script writes a simple markdown report to `target/e2e-report.md`.

Expected short output (successful run):

- Login returns a `token`.
- Each `validar-texto` call returns a success message.
- `GET /api/qrcode/progresso` shows `completo: true` and `lidos: 4`.
- Upload returns JSON with `id`, `fileName` and `url`.
- Apply frame returns success JSON with `arquivo` and `url`.

If you want, I can extend the scripts to:
- run inside CI (GitHub Actions) and upload `target/e2e-report.md` as an artifact;
- add retries/timeouts and richer assertions (exit with non-zero if checks fail);
- parameterize frames and number of photos to upload.
