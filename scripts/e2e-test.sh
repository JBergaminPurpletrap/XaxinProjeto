#!/usr/bin/env bash
# Simple e2e script for the Xaxin project (Linux/macOS)
JAR=${1:-target/xaxin-projeto-qrcode-1.0.0.jar}
PROFILE=${2:-h2}
BASEURL=${3:-http://localhost:8080}

wait_url() {
  url=$1; timeout=${2:-30}; i=0
  until curl -sSf "$url" >/dev/null 2>&1; do
    i=$((i+1))
    if [ $i -ge $timeout ]; then return 1; fi
    sleep 1
  done
  return 0
}

if [ -z "$NO_START" ]; then
  if [ ! -f "$JAR" ]; then echo "Jar not found: $JAR"; exit 1; fi
  java -jar "$JAR" --spring.profiles.active=$PROFILE &
  APP_PID=$!
  echo "Started app pid=$APP_PID"
  if ! wait_url "$BASEURL/teste.html" 40; then echo "app not ready"; kill $APP_PID; exit 1; fi
fi

USERNAME=tester
PASSWORD=123
NAME=Tester

# register (ignore errors)
curl -sS -X POST "$BASEURL/api/users/cadastro" -H 'Content-Type: application/json' -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\",\"nome\":\"$NAME\"}"

# login
TOKEN=$(curl -sS -X POST "$BASEURL/api/users/login" -H 'Content-Type: application/json' -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" | jq -r .token)
echo "TOKEN: $TOKEN"

# validate codes
for c in 1001 2002 3003 4004; do
  curl -sS -X POST "$BASEURL/api/qrcode/validar-texto" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d "{\"codigo\":\"$c\"}"
  echo
done

# progresso
curl -sS -H "Authorization: Bearer $TOKEN" "$BASEURL/api/qrcode/progresso" | jq

# create temp image and upload
IMG=temp_test_image.png
echo 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=' | base64 --decode > $IMG
UPLOAD=$(curl -sS -X POST "$BASEURL/api/album/upload" -H "Authorization: Bearer $TOKEN" -F "file=@$IMG")
echo "UPLOAD: $UPLOAD"
FOTOID=$(echo "$UPLOAD" | jq -r .id)

# apply frame
if [ "$FOTOID" != "null" ]; then
  curl -sS -X POST "$BASEURL/api/album/moldura/$FOTOID" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"frame":1}'
  echo
fi

# final album
curl -sS -H "Authorization: Bearer $TOKEN" "$BASEURL/api/album" | jq

if [ -n "$APP_PID" ]; then
  kill $APP_PID
fi

echo "Done"
