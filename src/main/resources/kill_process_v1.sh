#!/bin/bash

# Check if the correct number of arguments is provided
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <username> <process_name> <num_attempts>"
    exit 1
fi

username="$1"
process_name="$2"
num_attempts="$3"

# Check if the provided process name exists
if ! pgrep -U "$username" -x "$process_name" > /dev/null; then
    echo "No processes found with the name: $process_name for user: $username"
    exit 1
fi

# Attempt to kill the processes
for attempt in $(seq "$num_attempts"); do
    echo "Attempt $attempt to kill processes with the name: $process_name for user: $username"

    # Get all process IDs for the given process name and username
    pids=$(pgrep -U "$username" -x "$process_name")

    if [ -z "$pids" ]; then
        echo "No processes found with the name: $process_name for user: $username"
        exit 1
    fi

    # Kill all the processes
    for pid in $pids; do
        kill "$pid"
        echo "Killed process $pid"
    done

    # Wait for a short duration before checking again
    sleep 1

    # Check if processes still exist
    if pgrep -U "$username" -x "$process_name" > /dev/null; then
        echo "Processes with the name $process_name for user $username still exist after attempt $attempt"
    else
        echo "All processes with the name $process_name for user $username killed successfully"
        exit 0
    fi
done

echo "Failed to kill processes after $num_attempts attempts"
exit 1
