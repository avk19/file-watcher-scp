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

    while pgrep -U "$username" -x "$process_name" > /dev/null; do
        # Get all process IDs for the given process name and username
        pids=$(pgrep -U "$username" -x "$process_name")

        # Kill all the processes
        for pid in $pids; do
            kill "$pid"
            echo "Killed process $pid"
        done

        # Wait for a short duration before checking again
        sleep 1
    done

    echo "All processes with the name $process_name for user $username killed successfully after attempt $attempt"
done

echo "Failed to kill processes after $num_attempts attempts"
exit 1
