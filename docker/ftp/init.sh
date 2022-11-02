#!/bin/bash
for user_home in /home/*; do
  if [ -d "$user_home" ]; then
    username=$(basename $user_home)
    echo "Setup $user_home/share/request folder for $username"
    mkdir -p $user_home/share/request
    chown -R $username:users $user_home/share/request

    echo "Setup $user_home/share/response folder for $username"
    mkdir -p $user_home/share/response
    chown -R $username:users $user_home/share/response
  fi
done
