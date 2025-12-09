#!/bin/bash
export SPRING_DATASOURCE_PASSWORD=$(cat /app/db_password.txt)
exec java -jar /app/s4-backend-1.0.0.jar