#!/bin/bash
docker build -t jboss-demo-app:0.6 . 
docker run --rm -i -t -p 8080:8080 jboss-demo-app:0.6