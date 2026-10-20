#!/usr/bin/env bash
#
# Refreshes langfuse.yml to the OpenAPI specification of a Langfuse server release, and pins the
# default Dev Services / Testcontainers images to that same release.
#
# langfuse.yml is a verbatim copy of the published spec - it is never hand-edited. Everything the
# extension exposes is generated from it, so a refresh is expected to change the generated client
# and may remove operations Langfuse has retired.
#
# The spec and the default images always move together: the release whose spec is vendored here is
# the release recorded in LangfuseConfig.SERVER_VERSION, which both the web server and the worker
# default images are derived from.
#
# Usage:
#   ./update-spec.sh             # latest stable Langfuse release
#   ./update-spec.sh 4.65.0      # a specific release
#
# The spec is read from the release tag in github.com/langfuse/langfuse
# (web/public/generated/api/openapi.yml), which is the file Langfuse Cloud serves at
# https://cloud.langfuse.com/generated/api/openapi.yml. Set LANGFUSE_SPEC_URL to read it from somewhere
# else, for example a self-hosted deployment of the same release (<base-url>/api/openapi.yaml).
#
# After refreshing, rebuild and review the diff:
#   ./mvnw clean install -DskipTests
#   git diff --stat

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
readonly REPO_ROOT
readonly SPEC_FILE="${REPO_ROOT}/langfuse-client/src/main/openapi/langfuse.yml"
readonly VERSION_FILE="${REPO_ROOT}/langfuse-testcontainers/src/main/java/com/langfuse/testcontainers/config/LangfuseConfig.java"
readonly VERSION_PATTERN='^[0-9]+\.[0-9]+\.[0-9]+$'

resolve_latest_version() {
  curl --fail --silent --show-error --location \
    -H "Accept: application/vnd.github+json" \
    ${GITHUB_TOKEN:+-H "Authorization: Bearer ${GITHUB_TOKEN}"} \
    https://api.github.com/repos/langfuse/langfuse/releases/latest |
    sed -n 's/^ *"tag_name": *"v\{0,1\}\([^"]*\)".*/\1/p'
}

VERSION="${1:-}"

if [[ -z "${VERSION}" ]]; then
  VERSION="$(resolve_latest_version)"
  echo "Latest Langfuse release: ${VERSION}"
fi

VERSION="${VERSION#v}"

if [[ ! "${VERSION}" =~ ${VERSION_PATTERN} ]]; then
  echo "'${VERSION}' is not a Langfuse release version (expected MAJOR.MINOR.PATCH) - aborting" >&2
  exit 1
fi

readonly VERSION
readonly SPEC_URL="${LANGFUSE_SPEC_URL:-https://raw.githubusercontent.com/langfuse/langfuse/v${VERSION}/web/public/generated/api/openapi.yml}"

echo "Downloading ${SPEC_URL}"

TMP_FILE="$(mktemp)"
readonly TMP_FILE
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

perl -pi -e 's/(String SERVER_VERSION = ")[^"]*(";)/${1}'"${VERSION}"'${2}/' "${VERSION_FILE}"

if ! grep -q "String SERVER_VERSION = \"${VERSION}\";" "${VERSION_FILE}"; then
  echo "Could not set SERVER_VERSION in ${VERSION_FILE} - aborting" >&2
  exit 1
fi

echo "Updated ${SPEC_FILE}"
echo "Pinned the default Langfuse images to ${VERSION} in ${VERSION_FILE}"
echo "Review the diff, then rebuild with: ./mvnw clean install -DskipTests"
