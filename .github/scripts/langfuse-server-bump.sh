#!/usr/bin/env bash
#
# Looks for Langfuse server releases newer than LangfuseConfig.SERVER_VERSION and moves the extension
# to the newest one the build passes against.
#
# The vendored OpenAPI spec and the default Dev Services / Testcontainers images are pinned to the same
# release (see langfuse-client/src/main/openapi/update-spec.sh). For every candidate release, newest
# first, this script refreshes both to that release and runs the full build, including the integration
# tests against the candidate images:
#
#   - The first candidate that passes is proposed as a pull request.
#   - Every candidate that fails gets an issue (or a comment on its existing issue), and the next older
#     candidate is tried.
#
# Only stable releases of the current major version are considered, and at most MAX_ATTEMPTS candidates
# are built per run. Exits non-zero when no candidate passed.
#
# Requires: git, gh (authenticated through GH_TOKEN), jq, perl, curl and a container runtime.
#
# Environment:
#   GH_TOKEN          token for gh (contents, pull-requests and issues write)
#   GITHUB_REPOSITORY owner/name of this repository
#   BASE_BRANCH       branch the pull request targets (default: main)
#   MAX_ATTEMPTS      maximum number of candidate releases to build (default: 5)
#   RUN_URL           link to the CI run, used in issues and pull requests (optional)
#   LOG_DIR           where build logs are written (default: target/langfuse-server-bump)
#   MVN_ARGS          Maven arguments for the verification build
#   DRY_RUN           when "true", report what would be created instead of pushing / creating anything

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
readonly REPO_ROOT
readonly VERSION_FILE="langfuse-testcontainers/src/main/java/com/langfuse/testcontainers/config/LangfuseConfig.java"
readonly UPDATE_SPEC="langfuse-client/src/main/openapi/update-spec.sh"
readonly BASE_BRANCH="${BASE_BRANCH:-main}"
readonly MAX_ATTEMPTS="${MAX_ATTEMPTS:-5}"
readonly RUN_URL="${RUN_URL:-}"
readonly LOG_DIR="${LOG_DIR:-${REPO_ROOT}/target/langfuse-server-bump}"
readonly MVN_ARGS="${MVN_ARGS:--B formatter:validate clean install}"
readonly DRY_RUN="${DRY_RUN:-false}"
readonly ISSUE_TITLE_PREFIX="Build fails against Langfuse server"
readonly BRANCH_PREFIX="langfuse-server"
readonly BUMPED_PATHS=(langfuse-client/src/main/openapi/langfuse.yml "${VERSION_FILE}" docs/modules/ROOT/pages/includes)

cd "${REPO_ROOT}"

restore_pinned_files() {
  git checkout --quiet HEAD -- "${BUMPED_PATHS[@]}"
}

log() {
  echo "::notice::$*"
}

current_version() {
  sed -n 's/.*String SERVER_VERSION = "\([^"]*\)";.*/\1/p' "${VERSION_FILE}"
}

# Stable releases of the given major version that are newer than the given version, newest first.
candidate_versions() {
  local current="$1"
  local major="${current%%.*}"

  gh api "repos/langfuse/langfuse/releases?per_page=100" \
    --jq '.[] | select((.draft | not) and (.prerelease | not)) | .tag_name' |
    sed 's/^v//' |
    { grep -E "^${major}\.[0-9]+\.[0-9]+$" || true; } |
    { cat; echo "${current}"; } |
    sort -V -r -u |
    sed "/^${current//./\\.}$/,\$d"
}

existing_issue() {
  local version="$1"

  gh issue list --repo "${GITHUB_REPOSITORY}" --state open --search "\"${ISSUE_TITLE_PREFIX} ${version}\" in:title" \
    --json number,title --jq ".[] | select(.title == \"${ISSUE_TITLE_PREFIX} ${version}\") | .number" |
    head -1
}

open_pull_request() {
  local version="$1"

  gh pr list --repo "${GITHUB_REPOSITORY}" --state open --head "${BRANCH_PREFIX}/${version}" --json url --jq '.[0].url // empty'
}

failure_summary() {
  local build_log="$1"

  grep -E '<<< (FAILURE|ERROR)!|Tests run:.*(Failures: [1-9]|Errors: [1-9])|\[ERROR\] (Failed|Tests|COMPILATION|.*\.java)' "${build_log}" |
    sed 's/\x1b\[[0-9;]*m//g' |
    head -40 ||
    true
}

report_failure() {
  local version="$1"
  local build_log="$2"
  local issue
  local body

  issue="$(existing_issue "${version}")"
  body="$(cat <<EOF
The build fails when the vendored OpenAPI spec and the default Langfuse images are moved to [Langfuse ${version}](https://github.com/langfuse/langfuse/releases/tag/v${version}).

${RUN_URL:+CI run: ${RUN_URL}}

Reproduce locally:

\`\`\`shell
${UPDATE_SPEC} ${version}
./mvnw ${MVN_ARGS}
\`\`\`

Failures:

\`\`\`
$(failure_summary "${build_log}")
\`\`\`
EOF
)"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log "[dry-run] would ${issue:+comment on issue #${issue}}${issue:-create an issue} for ${version}"
  elif [[ -n "${issue}" ]]; then
    gh issue comment "${issue}" --repo "${GITHUB_REPOSITORY}" --body "${body}"
    log "Commented on issue #${issue} for ${version}"
  else
    gh issue create --repo "${GITHUB_REPOSITORY}" --title "${ISSUE_TITLE_PREFIX} ${version}" --label dependencies --body "${body}"
    log "Created an issue for ${version}"
  fi
}

propose_update() {
  local current="$1"
  local version="$2"
  local skipped="$3"
  local branch="${BRANCH_PREFIX}/${version}"
  local body

  body="$(cat <<EOF
Moves the vendored OpenAPI spec and the default Langfuse web server and worker images from ${current} to [Langfuse ${version}](https://github.com/langfuse/langfuse/releases/tag/v${version}).

The full build, including the integration tests against \`langfuse/langfuse:${version}\` and \`langfuse/langfuse-worker:${version}\`, passed${RUN_URL:+ in ${RUN_URL}}.
${skipped:+
Newer releases were skipped because the build fails against them:
${skipped}}

Generated by \`.github/scripts/langfuse-server-bump.sh\`.
EOF
)"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log "[dry-run] would push ${branch} and open a pull request against ${BASE_BRANCH}"
    git --no-pager diff --stat
  else
    git checkout -B "${branch}"
    git add "${BUMPED_PATHS[@]}"
    git commit -m "Move to Langfuse server ${version}" -m "Updates the vendored OpenAPI spec and the default Langfuse images from ${current} to ${version}."
    git push --force origin "${branch}"
    gh pr create --repo "${GITHUB_REPOSITORY}" --base "${BASE_BRANCH}" --head "${branch}" \
      --title "Move to Langfuse server ${version}" --label dependencies --body "${body}"
  fi
}

main() {
  local current
  local candidates
  local version
  local attempts=0
  local skipped=""

  current="$(current_version)"

  if [[ -z "${current}" ]]; then
    echo "Could not read SERVER_VERSION from ${VERSION_FILE}" >&2
    exit 1
  fi

  candidates="$(candidate_versions "${current}")"

  if [[ -z "${candidates}" ]]; then
    log "Already on the latest Langfuse ${current%%.*}.x release (${current})"
    exit 0
  fi

  log "Pinned to ${current}; newer releases: $(echo "${candidates}" | tr '\n' ' ')"
  mkdir -p "${LOG_DIR}"

  for version in ${candidates}; do
    local pr

    pr="$(open_pull_request "${version}")"

    if [[ -n "${pr}" ]]; then
      log "A pull request for ${version} is already open: ${pr}"
      exit 0
    fi

    if (( attempts >= MAX_ATTEMPTS )); then
      log "Tried ${MAX_ATTEMPTS} releases; stopping before ${version}"
      break
    fi

    attempts=$((attempts + 1))
    log "Building against Langfuse ${version}"

    restore_pinned_files
    "${UPDATE_SPEC}" "${version}"

    local build_log="${LOG_DIR}/build-${version}.log"

    # shellcheck disable=SC2086 # MVN_ARGS is a list of arguments
    if ./mvnw ${MVN_ARGS} > "${build_log}" 2>&1; then
      log "Build passed against ${version}"
      propose_update "${current}" "${version}" "${skipped}"
      exit 0
    fi

    log "Build failed against ${version}"
    report_failure "${version}" "${build_log}"
    skipped="${skipped}- ${version}"$'\n'
  done

  restore_pinned_files
  echo "The build did not pass against any newer Langfuse release" >&2
  exit 1
}

main "$@"
