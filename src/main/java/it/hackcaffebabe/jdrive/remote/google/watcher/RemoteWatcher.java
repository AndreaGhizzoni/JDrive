package it.hackcaffebabe.jdrive.remote.google.watcher;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.remote.google.DriveFileManager;
import it.hackcaffebabe.jdrive.remote.google.MIMEType;
import it.hackcaffebabe.jdrive.remote.google.RemoteToLocalFiles;
import it.hackcaffebabe.jdrive.remote.google.auth.GoogleAuthenticator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * TODO add doc
 */
public class RemoteWatcher implements Runnable
{
    private static final Logger log = LogManager.getLogger();
    private static RemoteWatcher instance;

    private Drive driveService;
    private RemoteToLocalFiles remoteToLocalFiles;
    private DriveFileManager driveFileManager;

    public static RemoteWatcher getInstance() throws GeneralSecurityException,
                                                     IOException {
        if( instance == null )
            instance = new RemoteWatcher();
        return instance;
    }

    private RemoteWatcher() throws GeneralSecurityException, IOException {
        driveService = GoogleAuthenticator.getInstance().getDriveService();
        remoteToLocalFiles = RemoteToLocalFiles.getInstance();
        driveFileManager = DriveFileManager.getInstance();

        Path jdriveLocalBasePath = Paths.get(
            (String) Configurator.getInstance().get( Keys.WATCHED_BASE_PATH )
        );
        File jdriveRemoteFolder = getJDriveRemoteFolderOrCreate( jdriveLocalBasePath );
        remoteToLocalFiles.put( jdriveRemoteFolder, jdriveLocalBasePath );
        log.info("JDrive remote folder found.");

        remoteToLocalFiles.putAll( recursivelyGetFrom( jdriveRemoteFolder.getId() ) );
        log.info("Mapping remote and local file complete.");
    }

    @Override
    public void run() {
        // while(keepRunning) {
            // actualRemoteFiles = recursivelyGetFrom JDrive remote folder
            // check the differences: actualRemoteFiles and RemoteToLocal.

            // if some difference has been found
            //     dispatch each difference through some queue...
            // else
            //     sleep for some time
        //}
    }

    private File getJDriveRemoteFolderOrCreate( Path jdriveLocalBasePath ) throws IOException {
        String queryPattern = "mimeType = '%s' and not trashed and "+
                              "'root' in parents and name = 'Google Drive'";
        String query = String.format( queryPattern, MIMEType.GOOGLE_FOLDER );

        List<File> result = doQuery( query );
        if( result.isEmpty() ){
            return driveFileManager.createRemoteFolderFrom( jdriveLocalBasePath );
        }else if( result.size() > 1 ){
            throw new IOException( "Multiple JDrive remote folder found." );
        }

        return result.get(0);
    }

    private HashMap<File, Path> recursivelyGetFrom( String remoteParentsId ) throws IOException {
        String q = String.format("not trashed and '%s' in parents", remoteParentsId );
        HashMap<File, Path> folderContent = new HashMap<>();
        doQuery( q ).forEach(
            file -> {
                DriveFileManager.logFile( file );
                folderContent.put( file, null );
                try {
                    if( file.getMimeType().equals(MIMEType.GOOGLE_FOLDER) ){
                        folderContent.putAll( recursivelyGetFrom(file.getId()) );
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        );
        return folderContent;
    }

    private List<File> doQuery( String query ) throws IOException {
        Drive.Files.List request = driveService.files().list()
            .setQ( query )
            .setFields("files(id, name, parents, mimeType, kind, size, modifiedTime)")
            .setSpaces("drive");

        List<File> result = new ArrayList<>();
        do {
            FileList files = request.execute();
            result.addAll(files.getFiles());

            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }

    // TODO add closing procedure that will be performed by ActionServer
}