docker ps -a | grep 'lcm' | awk '{print $1}' | xargs --no-run-if-empty docker rm
