#!/usr/bin/env bash
set -euo pipefail
# Hardened e2e script for the Xaxin project (Linux/macOS)
JAR=${1:-target/xaxin-projeto-qrcode-1.0.0.jar}
PROFILE=${2:-h2}
BASEURL=${3:-http://localhost:8080}

mkdir -p target

wait_url() {
  url=$1; timeout=${2:-60}; i=0
  until curl -sSf "$url" >/dev/null 2>&1; do
    i=$((i+1))
    if [ $i -ge $timeout ]; then return 1; fi
    sleep 2
  done
  return 0
}

if [ -z "${NO_START-}" ]; then
  if [ ! -f "$JAR" ]; then echo "Jar not found: $JAR"; exit 1; fi
  java -jar "$JAR" --spring.profiles.active=$PROFILE > target/app.out 2> target/app.err &
  APP_PID=$!
  echo $APP_PID > app.pid
  echo "Started app pid=$APP_PID"
  if ! wait_url "$BASEURL/actuator/health" 60; then echo "app not ready"; cat target/app.err || true; kill $APP_PID || true; exit 1; fi
fi

USERNAME=tester
PASSWORD=123
NAME=Tester

# register (ignore errors)
curl -sS -X POST "$BASEURL/api/users/cadastro" -H 'Content-Type: application/json' -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\",\"nome\":\"$NAME\"}" -o target/smoke-register.json || true

# login: support both envelope .data.token and legacy .token
curl -sS -X POST "$BASEURL/api/users/login" -H 'Content-Type: application/json' -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" -o target/smoke-login.json
TOKEN=$(jq -r '.data.token // .token // empty' target/smoke-login.json || true)
echo "TOKEN: $TOKEN" > target/smoke-token.txt
if [ -z "$TOKEN" ]; then cat target/smoke-login.json; echo "login failed"; kill ${APP_PID-} || true; exit 1; fi

# validate codes
for c in 1001 2002 3003 4004; do
  curl -sS -X POST "$BASEURL/api/qrcode/validar-texto" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d "{\"codigo\":\"$c\"}" -o target/smoke-validate-$c.json || true
done

# progresso (validate structure)
curl -sS -H "Authorization: Bearer $TOKEN" "$BASEURL/api/qrcode/progresso" -o target/smoke-progresso.json
if ! jq -e '.data // . | (has("total") or has("checkpoints"))' target/smoke-progresso.json >/dev/null; then
  echo "progresso schema unexpected:"; cat target/smoke-progresso.json; kill ${APP_PID-} || true; exit 1
fi

# create temp image and upload
IMG=temp_test_image.png
echo 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=' | base64 --decode > $IMG
curl -sS -X POST "$BASEURL/api/album/upload" -H "Authorization: Bearer $TOKEN" -F "file=@$IMG" -o target/smoke-upload.json || true
echo "UPLOAD:"; cat target/smoke-upload.json
FOTOID=$(jq -r '.data.id // .id // empty' target/smoke-upload.json || true)

# apply frame (if upload returned id)
if [ -n "$FOTOID" ]; then
  curl -sS -X POST "$BASEURL/api/album/moldura/$FOTOID" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"frame":1}' -o target/smoke-frame.json || true
  echo "FRAME:"; cat target/smoke-frame.json
fi

# final album
curl -sS -H "Authorization: Bearer $TOKEN" "$BASEURL/api/album" -o target/smoke-album.json || true
jq . target/smoke-album.json || true

if [ -n "${APP_PID-}" ]; then
  kill $APP_PID || true
fi

echo "Done"
