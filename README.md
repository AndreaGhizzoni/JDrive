# Google Drive file sync in Java.

## Conceptual Idea
JDrive is proposing as a linux synchronization tool for your Google 
Drive documents (like Dropbox).
JDrive only needs to have access to your Google Drive, via standard 
Google OAuth2 authentication process, in order to keep your file sync.

## Dependencies
- Java 1.7
- Gradle 2.12

## Code
All the project is written using the latest version of IntelliJ as
Gradle project, so to Import it using `gradle import`.

## Building from this repo
After correctly installed all the dependencies, run:
```bash
git clone git@github.com:AndreaGhizzoni/JDrive.git JDrive
cd JDrive
gradle
```
Running only `gradle` is enough to build all the project.
The last command produce a folder in `build/install` called `JDrive`
with the following content:
- lib: witch contains all the dependencies jar
- bin: witch contains the script to launch JDrive, depending to your OS

## Usage
The following instructions assumes that your under unix-like OS and you
have build it from this repo.

To start JDrive follow the instruction below:
```bash
./build/install/JDrive/bin/JDrive -start
```

To stop the current instance of JDrive:
```bash
./build/install/JDrive/bin/JDrive -stop
```

To get the current status:
```bash
./build/install/JDrive/bin/JDrive -ststus
```

and for the complete list of commands:
```bash
./build/install/JDrive/bin/JDrive -help
```
