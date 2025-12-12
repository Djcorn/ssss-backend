#!/bin/bash
export SPRING_DATASOURCE_PASSWORD=$(cat /app/db_password.txt)
export GOOGLE_ACCOUNT_PUBLIC_KEY=$(cat /app/google_public_key.txt)
exec java -jar /app/s4-backend-1.0.0.jar