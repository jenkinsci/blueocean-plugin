#!/usr/bin/env bash
set -eu -o pipefail

HERE="$(cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"

new_build_container() {
  local build_image=$1; shift

  build_container=$(docker create -i -v "$HERE":/build -w /build "$build_image" /bin/cat)
  echo "$build_container" > "$HERE/.build_container"
}

delete_build_container() {
  docker rm "$build_container"
  rm "$HERE/.build_container"
}

stop_build_container() {
  echo "* Stopping build container $build_container"
  local error; error=$(docker kill "$build_container")
  if [[ $? -ne 0 ]]; then echo "$error"; exit 1; fi
  trap EXIT
}

prepare_build_container() {
  local build_image=$1; shift
  if [[ -f $HERE/.build_container ]]; then
    read -r build_container < "$HERE/.build_container"
  fi

  local state; state=$(docker inspect --format="{{ .State.Status }}" "$build_container")
  if [[ $? -eq 0 ]]; then
    if [[ $state != "exited" ]];
    then
      echo "ERROR: build container $build_container is not in a re-usable state, is there another build running?"
      exit 1
    fi

    local image_id; image_id=$(docker inspect --format="{{ .Id }}" "$build_image")
    local container_image_id; container_image_id=$(docker inspect --format="{{ .Image }}" "$build_container")

    # was the image updated manually with a docker pull?
    if [[ "$image_id" != "$container_image_id" ]]; then
      delete_build_container
      new_build_container "$build_image"
    fi
  else
    new_build_container "$build_image"
  fi

  trap stop_build_container EXIT
}

# simulate Jenkins Pipeline docker.image().inside {}
build_inside() {
  local build_image=$1; shift
  local commands=$1; shift
  prepare_build_container "$build_image"
  local error; error=$(docker start "$build_container")
  if [[ $? -ne 0 ]]; then echo "$error"; exit 1; fi
  echo "Starting a new build inside container $build_container"
  echo "$commands" | docker exec -i -u "$(id -u)" "$build_container" /bin/bash
  stop_build_container
}

commands="mvn clean install -B -DcleanNode -Dmaven.test.failure.ignore"
if [[ $# -ne 0 ]]; then commands="$*"; fi

build_inside "cloudbees/java-build-tools" "$commands"
