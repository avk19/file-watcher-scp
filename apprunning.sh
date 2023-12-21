#!/bin/bash

# Define the name of your Java application JAR file
APP_JAR="your_application.jar"

# Function to check if the application is running
is_app_running() {
    local app_pid
    app_pid=$(ps aux | grep "$APP_JAR" | grep -v grep | awk '{print $2}')
    if [ -n "$app_pid" ]; then
        return 0  # Application is running
    else
        return 1  # Application is not running
    fi
}

# Check if the application is running
if is_app_running; then
    echo "Application is already running."
else
    echo "Application is not running. Starting..."
    # Change directory to where your JAR file is located
    cd /path/to/your/application
    
    # Start the Java application
    nohup java -jar "$APP_JAR" /path/to/config.properties > app.log 2>&1 &
    echo "Application started."
fi
