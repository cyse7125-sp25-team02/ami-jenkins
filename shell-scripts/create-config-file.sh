#!/bin/bash
set -x

sudo tee /etc/nginx/sites-available/jenkins.jkops.me <<'EOF'
upstream jenkins {
    server 127.0.0.1:8080;
}

server {
    listen      80;
    server_name jenkins.jkops.me;

    access_log  /var/log/nginx/jenkins.access.log;
    error_log   /var/log/nginx/jenkins.error.log;

    proxy_buffers 16 64k;
    proxy_buffer_size 128k;

    location / {
        proxy_pass  http://jenkins;
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
        proxy_redirect off;

        proxy_set_header    Host            $host;
        proxy_set_header    X-Real-IP       $remote_addr;
        proxy_set_header    X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header    X-Forwarded-Proto https;
    }
}
EOF

sudo ln -s /etc/nginx/sites-available/jenkins.jkops.me /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
