#!/usr/bin/env bash
#
# scripts/perf_test.sh
# Simple concurrent load test using curl -k to ignore self-signed certs.
# Usage: ./perf_test.sh [URL] [TOTAL_REQUESTS] [CONCURRENCY]

set -euo pipefail

URL="${1:-https://localhost:8443/app-download}"
TOTAL="${2:-200}"
CONCURRENCY="${3:-20}"
REQS_PER=$(( TOTAL / CONCURRENCY ))
LATENCIES="latencies.txt"

echo "Running $TOTAL requests to $URL with $CONCURRENCY concurrent workers"
> "$LATENCIES"

for _ in $(seq 1 "$CONCURRENCY"); do
  (
    for _ in $(seq 1 "$REQS_PER"); do
      # -k to skip cert validation
      t=$(curl -k -s -o /dev/null -w "%{time_total}" "$URL")
      # convert to milliseconds (rounded)
      ms=$(awk -v sec="$t" 'BEGIN { printf "%.0f", sec * 1000 }')
      echo "$ms" >> "$LATENCIES"
    done
  ) &
done

wait

if [[ ! -s "$LATENCIES" ]]; then
  echo "ERROR: No latencies recorded. Is the server running at $URL?"
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
