#!/bin/bash

# Check if folder name is provided as an argument
if [ -z "$1" ]; then
    echo "Please provide the folder name containing the Java program."
    exit 1
fi

PROGRAM_DIR="$1"

# Check if the folder exists
if [ ! -d "$PROGRAM_DIR" ]; then
    echo "Directory '$PROGRAM_DIR' does not exist."
    exit 1
fi

# Create the build directory if it doesn't exist
BUILD_DIR="build"
mkdir -p "$BUILD_DIR"

# Compile all Java files recursively, keeping the package structure
echo "Compiling Java files in '$PROGRAM_DIR'..."
find "$PROGRAM_DIR" -name "*.java" > sources.txt
javac -d "$BUILD_DIR" @sources.txt
rm sources.txt

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful."
    
    # Run Main class
    if [ -f "$BUILD_DIR/Jaikin.class" ]; then
        echo "Running Jaikin..."
        java -cp "$BUILD_DIR" Jaikin
    else
        echo "Jaikin class not found. Make sure Jaikin.java is in the root of '$PROGRAM_DIR'."
        exit 1
    fi
else
    echo "Compilation failed."
    exit 1
fi