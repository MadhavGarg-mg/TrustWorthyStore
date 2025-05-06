#!/usr/bin/env bash
#
# scripts/upload_perf_test.sh
# Concurrent upload load test for the upload endpoint.
# Usage: ./scripts/upload_perf_test.sh [URL] [TOTAL_REQUESTS] [CONCURRENCY] [FILE_PATH]

set -euo pipefail

URL="${1:-https://localhost:8443/app-upload}"
TOTAL="${2:-200}"
CONCURRENCY="${3:-20}"
FILE_PATH="${4:-sample.apk}"
APP_NAME="TestApp"
VERSION="1.0"
DEVELOPER="test"

if [[ ! -f "$FILE_PATH" ]]; then
  echo "ERROR: file not found: $FILE_PATH" >&2
  exit 1
fi

REQS_PER=$(( TOTAL / CONCURRENCY ))
LATENCIES="upload_latencies.txt"

echo "Running $TOTAL upload requests to $URL with $CONCURRENCY workers, $REQS_PER each"
> "$LATENCIES"

for _ in $(seq 1 "$CONCURRENCY"); do
  (
    for _ in $(seq 1 "$REQS_PER"); do
      # -sS silences progress but shows errors, -k ignores certs, -w prints time
      t=$(curl -k -sS -o /dev/null -w "%{time_total}" \
        -F "file=@${FILE_PATH}" \
        -F "appName=${APP_NAME}" \
        -F "version=${VERSION}" \
        -F "developer=${DEVELOPER}" \
        "$URL" ) || {
          echo "curl failed on $_/$REQS_PER in worker $$" >&2
          continue
        }
      ms=$(awk -v sec="$t" 'BEGIN { printf "%.0f", sec * 1000 }')
      echo "$ms" >> "$LATENCIES"
    done
  ) &
done

wait

if [[ ! -s "$LATENCIES" ]]; then
  echo "ERROR: No latencies recorded. Check the URL and file path again." >&2
  exit 1
fi

mapfile -t SORTED < <(sort -n "$LATENCIES")
COUNT=${#SORTED[@]}
MIN=${SORTED[0]}
MAX=${SORTED[$((COUNT - 1))]}
SUM=$(awk '{ s += $1 } END { print s }' "$LATENCIES")
AVG=$(awk -v total="$SUM" -v n="$COUNT" 'BEGIN { printf "%.2f", total/n }')
THROUGHPUT=$(awk -v n="$COUNT" -v sum="$SUM" 'BEGIN { printf "%.2f", n/(sum/1000) }')

echo
echo "Requests:     $COUNT"
echo "Min latency:  ${MIN} ms"
echo "Max latency:  ${MAX} ms"
echo "Avg latency:  ${AVG} ms"
echo "Throughput:   ${THROUGHPUT} req/s"
