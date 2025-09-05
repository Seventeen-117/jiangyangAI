#!/usr/bin/env bash
set -euo pipefail

MS_URL="${MS_URL:-}" 
MS_TOKEN="${MS_TOKEN:-}"
PROJECT_ID="${PROJECT_ID:-}"
SERVICE_NAME="${SERVICE_NAME:-}"
BRANCH="${BRANCH:-unknown}"
COMMIT="${COMMIT:-unknown}"
REPORT_FILE="${REPORT_FILE:-messages-service/target/site/jacoco/jacoco.xml}"

if [[ -z "$MS_URL" || -z "$MS_TOKEN" || -z "$PROJECT_ID" || -z "$SERVICE_NAME" ]]; then
  echo "Missing required env: MS_URL MS_TOKEN PROJECT_ID SERVICE_NAME" >&2
  exit 1
fi

if [[ ! -f "$REPORT_FILE" ]]; then
  echo "Report file not found: $REPORT_FILE" >&2
  exit 2
fi

echo "Uploading report to ${MS_URL}/api/whitebox/upload ..."
curl -sfS -X POST "${MS_URL}/api/whitebox/upload" \
  -H "Authorization: Bearer ${MS_TOKEN}" \
  -F "projectId=${PROJECT_ID}" \
  -F "serviceName=${SERVICE_NAME}" \
  -F "branch=${BRANCH}" \
  -F "commit=${COMMIT}" \
  -F "report=@${REPORT_FILE};type=text/xml"
echo "\nUpload success"

