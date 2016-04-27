# Google Drive desktop integration in Java.

## Conceptual Idea
JDrive is proposing as a linux client sync for your Google Drive 
documents (like Dropbox).
JDrive only needs to have access to your Google Drive, via standard 
Google OAuth2 authentication process, in order to keep sync your file.
   
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