#!/usr/bin/env bash
#
# Refreshes langfuse.yml from the upstream Langfuse OpenAPI specification.
#
# langfuse.yml is a verbatim copy of the published spec - it is never hand-edited. Everything the
# extension exposes is generated from it, so a refresh is expected to change the generated client
# and may remove operations Langfuse has retired.
#
# Usage:
#   ./update-spec.sh                 # refresh from Langfuse Cloud
#   LANGFUSE_SPEC_URL=... ./update-spec.sh   # refresh from a self-hosted deployment
#
# A self-hosted deployment publishes its spec at <base-url>/api/openapi.yaml.
#
# After refreshing, rebuild and review the diff:
#   ./mvnw -pl langfuse-client install -DskipTests
#   git diff --stat langfuse-client/src/main/openapi/langfuse.yml

set -euo pipefail

readonly SPEC_URL="${LANGFUSE_SPEC_URL:-https://cloud.langfuse.com/generated/api/openapi.yml}"
readonly SPEC_FILE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/langfuse.yml"

echo "Downloading ${SPEC_URL}"

readonly TMP_FILE="$(mktemp)"
trap 'rm -f "${TMP_FILE}"' EXIT

curl --fail --silent --show-error --location --output "${TMP_FILE}" "${SPEC_URL}"

if [[ ! -s "${TMP_FILE}" ]]; then
  echo "Downloaded specification is empty - aborting" >&2
  exit 1
fi

if ! head -1 "${TMP_FILE}" | grep -q '^openapi:'; then
  echo "Downloaded file does not look like an OpenAPI specification - aborting" >&2
  exit 1
fi

mv "${TMP_FILE}" "${SPEC_FILE}"
trap - EXIT

echo "Updated ${SPEC_FILE}"
echo "Review the diff, then rebuild with: ./mvnw -pl langfuse-client install -DskipTests"
