package it.hackcaffebabe.jdrive.remote.google.watcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TODO add doc
 */
public class RemoteWatcher implements Runnable
{
    private static final Logger log = LogManager.getLogger();

    public RemoteWatcher() {
        // get drive services
        // get remote to local map instance
        // get DriveFileManager instance

        // call getJDriveRemoteFolderOrCreate()
        // populate the map with recursivelyListFrom JDrive remote folder.
    }

    @Override
    public void run() {
        // while(keepRunning) {
            // actualRemoteFiles = recursivelyListFrom JDrive remote folder
            // check the differences: actualRemoteFiles and RemoteToLocal.

            // if some difference has been found
            //     dispatch each difference through some queue...
            // else
            //     sleep for some time
        //}
    }

    // TODO add closing procedure that will be performed by ActionServer
}
