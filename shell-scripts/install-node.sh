#!/bin/bash
set -x

sudo apt update -y
sudo apt install nodejs npm -y
sudo npm install -g @commitlint/cli @commitlint/config-conventional
