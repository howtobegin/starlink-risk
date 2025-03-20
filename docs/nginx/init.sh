#!/bin/bash

cd ./data/nginx

cp -rf ./conf/slr-event-prod.conf /usr/local/nginx/conf/conf.d
cp -rf ./html/index.html /usr/local/nginx/html

/usr/local/nginx/sbin/nginx -t
/usr/local/nginx/sbin/nginx -s reload
