#!/usr/bin/env bash
set -eu -o pipefail

PROJECT_ROOT="$(cd -P "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

usage() {
  cat <<EOF
usage: $(basename "$0") COMMAND ARGS
Helper script to query Git
Commands:
  pr-id      Find the Pull Request id
             Assume that 'git fetch --progress https://github.com/jenkinsci/blueocean-plugin.git +refs/pull/*/head:refs/remotes/origin/pr/*' already ran

EOF
  exit 0
}

pr-id() {
  local github_remote=origin
  local head=$(git rev-parse --verify HEAD)
  git show-ref | sed -n "s|^$head refs/remotes/${github_remote}/pr/\(.*\)$|\1|p"
}

command_name="$1"; shift; case "$command_name" in
  pr-id)
    pr-id "$@"
    ;;
  *)
    usage
esac
