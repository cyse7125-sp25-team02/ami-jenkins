#!/bin/bash
set -x

sudo apt update -y
curl -fsSL https://deb.nodesource.com/setup_current.x | sudo -E bash -
sudo apt install nodejs -y
sudo npm install -g @commitlint/cli @commitlint/config-conventional
