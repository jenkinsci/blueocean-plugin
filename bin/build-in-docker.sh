#!/usr/bin/env bash
set -eu -o pipefail

PROJECT_ROOT="$(cd -P "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

setup_nice_output() {

  bold=""
  underline=""
  standout=""
  normal=""
  black=""
  red=""
  green=""
  yellow=""
  blue=""
  magenta=""
  cyan=""
  white=""

  # check if stdout is a terminal...
  if [ -t 1 ]; then

    # see if it supports colors...
    ncolors=$(tput colors)

    if test -n "$ncolors" && test $ncolors -ge 8; then
        bold="$(tput bold)"
        underline="$(tput smul)"
        standout="$(tput smso)"
        normal="$(tput sgr0)"
        black="$(tput setaf 0)"
        red="$(tput setaf 1)"
        green="$(tput setaf 2)"
        yellow="$(tput setaf 3)"
        blue="$(tput setaf 4)"
        magenta="$(tput setaf 5)"
        cyan="$(tput setaf 6)"
        white="$(tput setaf 7)"
    fi
  fi
}

new_build_container() {
  local build_image=$1; shift

  build_container=$(docker create -i -v "$PROJECT_ROOT":/build -w /build "$build_image" /bin/cat)
  echo "$build_container" > "$PROJECT_ROOT/.build_container"
}

delete_build_container() {
  docker rm "$build_container"
  rm "$PROJECT_ROOT/.build_container"
}

stop_build_container() {
  echo "${yellow}=> ${normal}Stopping build container $build_container"
  local error; error=$(docker kill "$build_container")
  if [[ $? -ne 0 ]]; then echo "$error"; exit 1; fi
  trap EXIT
}

stop_trap() {
  stop_build_container
  exit 1
}

prepare_build_container() {
  local build_image=$1; shift
  if [[ -f $PROJECT_ROOT/.build_container ]]; then
    read -r build_container < "$PROJECT_ROOT/.build_container"
  else
    new_build_container "$build_image"
    return
  fi

  if [[ "$clean" = true ]]; then
    echo "${yellow}=> ${normal}Removing old build container ${build_container}"
    docker kill "$build_container" || true
    docker rm "$build_container" || true
    rm "$PROJECT_ROOT/.build_container"
  else
    local state; state=$(docker inspect --format="{{ .State.Status }}" "$build_container")
    if [[ $? -ne 0 || "$state" != "exited" ]]; then
    echo "${red}ERROR: ${normal}Build container $build_container is not in a re-usable state, is there another build running?"
    exit 1
    fi

    # was the build image updated manually with a docker pull?
    local image_id; image_id=$(docker inspect --format="{{ .Id }}" "$build_image")
    local container_image_id; container_image_id=$(docker inspect --format="{{ .Image }}" "$build_container")
    if [[ "$image_id" != "$container_image_id" ]]; then
    echo "${yellow}WARNING ${normal}Build container is not using the latest available image. Consider using '-c' to get a fresh build container using latest image"
    fi
  fi

  new_build_container "$build_image"
}

# simulate Jenkins Pipeline docker.image().inside {}
build_inside() {
  local build_image=$1; shift
  prepare_build_container "$build_image"

  trap stop_trap EXIT
  echo "${yellow}=> ${normal}Starting build container ${build_container}"
  local error; error=$(docker start "$build_container")
  if [[ $? -ne 0 ]]; then echo "$error"; exit 1; fi

  echo "${yellow}=> ${normal}Launching ${cyan}'${build_commands}'${normal}"
  echo "$build_commands" | docker exec -i -u "$(id -u)" "$build_container" /bin/bash
  stop_build_container
}

build-git-description() {
  local head="$(git rev-parse --verify HEAD)"
  echo "BlueOcean plugins built from commit <a href=\"https://github.com/jenkinsci/blueocean-plugin/commit/${head}\">${head}</a>"
  local pr="$(git show-ref | sed -n "s|^$head refs/remotes/.*/pr/\(.*\)$|\1|p")"
  if [[ ! -z $pr ]]; then
      echo ", <a href=\"https://github.com/jenkinsci/blueocean-plugin/pull/${pr}\">Pull Request ${pr}</a><br>"
  fi
}

make_image() {
  echo "${yellow}=> ${normal}Building BlueOcean docker development image ${tag_name}"
  (cd "$PROJECT_ROOT" && docker build -t "$tag_name" . )
}

build_commands="mvn clean install -B -DcleanNode -Dmaven.test.failure.ignore"
tag_name="blueocean-dev:local"

usage() {
cat <<EOF
usage: $(basename $0) [-c|--clean] [-m|--make-image[=tag_name]] [-g|--git-data] [-h|--help] [BUILD_COMMAND]

  Build BlueOcean plugin suite locally like it would be in Jenkins, by isolating the build
  inside a Docker container. Requires a local Docker daemon to work.

  Create a BlueOcean docker dev image with Dockerfile if '-m' is passed and inject git revision data
  to it if '-g' is passed.

  In order to speed up builds, the build container is kept between builds in order to keep
  Maven / NPM caches. It can be cleaned up with '-c' option.

 BUILD_COMMAND    Commands used to build BlueOcean in the build container. Defaults to "$build_commands"
 tag_name         Tag name of the build. Defaults to "$tag_name"

EOF
  exit 0
}

clean=false
make_image=false
git_data=false

for i in "$@"; do
    case $i in
        -h|--help)
        usage
        ;;
        -c|--clean)
        clean=true
        shift # past argument=value
        ;;
        -m=*|--make-image=*)
        make_image=true
        tag_name="${i#*=}"
        shift # past argument=value
        ;;
        -m|--make-image)
        make_image=true
        shift # past argument=value
        ;;
        -g|--git-data)
        git_data=true
        shift # past argument=value
        ;;
        *)
        break
        ;;
    esac
done

if [[ $# -ne 0 ]]; then build_commands="$*"; fi

setup_nice_output
build_inside "cloudbees/java-build-tools"
if [[ "$git_data" = true ]]; then
  mkdir -p "$PROJECT_ROOT/docker/ref/init.groovy.d"
  cat > "$PROJECT_ROOT/docker/ref/init.groovy.d/build_data.groovy" <<EOF
jenkins.model.Jenkins.instance.setSystemMessage('''$(build-git-description)''')
EOF
fi

if [[ "$make_image" = true ]]; then
  make_image
fi
