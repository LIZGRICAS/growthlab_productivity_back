#!/usr/bin/env bash
set -euo pipefail

# Load .env if present
if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

if [ -z "${CLEVERTAP_ACCOUNT_ID:-}" ] || [ -z "${CLEVERTAP_TOKEN:-}" ]; then
  echo "CLEVERTAP credentials not set; skipping QA integration test."
  exit 0
fi

if [[ "${CLEVERTAP_ACCOUNT_ID}" == TEST-* ]] || [[ "${CLEVERTAP_TOKEN}" == TEST-* ]]; then
  echo "Detected placeholder CLEVERTAP credentials; skipping QA integration test."
  exit 0
fi

echo "Running QA integration test against CleverTap sandbox..."
mvn -Dtest=com.example.clevertap.integration.QAIntegrationTest test
