# Google Drive file sync in Java.

## Conceptual Idea
JDrive is proposing as a linux synchronization tool for your Google 
Drive documents (like Dropbox).
JDrive only needs to have access to your Google Drive, via standard 
Google OAuth2 authentication process, in order to keep your file sync.

## Dependencies
- Java 1.7
- Gradle 2.12

## Building from this repo
After correctly installed all the dependencies, run:
```bash
git clone git@github.com:AndreaGhizzoni/JDrive.git JDrive
cd JDrive
gradle build
```
The last command produce a runnable jar into `build/libs`.

## Usage
To start JDrive follow the instruction below:
```bash
java -jar JDrive-x.x.x.jar -start
```

To stop the current instance of JDrive:
```bash
java -jar JDrive-x.x.x.jar -stop
```

To get the current status:
```bash
java -jar JDrive-x.x.x.jar -status
```

and for the complete list of commands:
```bash
java -jar JDrive-x.x.x.jar -help
```

## Code
All the project is written using the latest version of IntelliJ as
Gradle project, so to Import it using `gradle import`.