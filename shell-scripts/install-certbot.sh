#!/bin/bash
set -x

sudo apt update -y
sudo apt upgrade -y
sudo apt install certbot python3-certbot-nginx -y
