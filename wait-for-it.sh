#!/bin/bash

set -e

host="$1"
shift
cmd="$@"

while !</dev/tcp/"$host"; do
  sleep 1
done

>&2 echo "$host is up - executing command"
exec $cmd
