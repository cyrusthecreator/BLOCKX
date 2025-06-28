const { exec } = require('child_process');
const fs = require('fs');
const path = require('path');
let packageData;
try {
    packageData = require('../package.json');
} catch (error) {
    packageData = {
        name: 'BlockX-Plugin',
        version: '1.0.0',
    };
}
// Command to run Gradle build
const gradleBuildCommand = 'gradle build';
// Root project path (current working directory + ../)
const rootProjectPath = path.join(process.cwd());

// Static path for the JAR file
const jarFilePath = path.join(rootProjectPath, 'build/libs/blockx-1.0.jar');

// Get destination folder from command-line arguments
const destinationFolder = process.argv[2];

function runGradleBuild() {
    return new Promise((resolve, reject) => {
        exec(gradleBuildCommand, { cwd: rootProjectPath }, (error, stdout, stderr) => {
            if (error) {
                reject(`Error: ${error.message}`);
                return;
            }
            if (stderr) {
                console.error(`Gradle stderr: ${stderr}`);
            }
            console.log(stdout);
            resolve();
        });
    });
}

function moveJarFile() {
    return new Promise((resolve, reject) => {
        if (fs.existsSync(jarFilePath)) {
            const destinationPath = path.join(destinationFolder, `${packageData.name}-${packageData.version}.jar`);
            fs.rename(jarFilePath, destinationPath, (err) => {
                if (err) {
                    reject(`📛 Error moving file: ${err}`);
                    return;
                }
                console.log(`✔ JAR file moved to: ${destinationPath}`);
                resolve();
            });
        } else {
            reject('📛 JAR file not found.');
        }
    });
}

async function buildAndMoveJar() {
    try {
        console.log('Running Gradle build...');
        await runGradleBuild();
        console.log('Build complete. Moving JAR file...');
        await moveJarFile();
        console.log('Operation completed successfully.');
    } catch (error) {
        console.error('Error:', error);
    }
}

// Check if destination folder argument is provided
if (!destinationFolder) {
    console.error('📛 Please provide a destination folder as an argument.');
    process.exit(1);
}

buildAndMoveJar();
