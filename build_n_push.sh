#!/bin/sh

docker-compose build
docker-compose push
docker-compose rm -f
docker images -q | xargs docker rmi 2>/dev/null
