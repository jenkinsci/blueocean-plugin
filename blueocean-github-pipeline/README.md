# blueocean-github-pipeline

## Running the proxy

1. Install nginx `brew tap homebrew/nginx && brew install nginx-full --with-sub`
2. `cd blueocean-github-pipeline`
3. `./run_proxy.sh`

Github.com should be proxied at `http://localhost:9000/api/v3/`
