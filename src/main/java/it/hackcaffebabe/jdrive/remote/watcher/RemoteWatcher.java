package it.hackcaffebabe.jdrive.remote.watcher;

import static it.hackcaffebabe.jdrive.Launcher.setPidToThreadContext;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.events.Event;
import it.hackcaffebabe.jdrive.mapping.MappedFileSystem;
import it.hackcaffebabe.jdrive.mapping.Mapper;
import it.hackcaffebabe.jdrive.remote.google.DriveFileManager;
import it.hackcaffebabe.jdrive.remote.google.MIMEType;
import it.hackcaffebabe.jdrive.remote.google.auth.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.remote.watcher.events.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TODO add doc
 */
public class RemoteWatcher
{
    private static final Logger log = LogManager.getLogger();
    private static RemoteWatcher instance;

    private Drive driveService;
    private DriveFileManager driveFileManager;

    private LinkedBlockingQueue<Event> dispatchingQueue;

    private Thread thread = new Thread( new Runnable() {
        private final Logger log = LogManager.getLogger();
        @Override
        public void run() {
            setPidToThreadContext();

            try {
                if (dispatchingQueue == null)
                    throw new IOException("Dispatching Queue missing");

                log.info("RemoteWatcher is started.");
                //while( !Thread.interrupted() ) {
                // actualRemoteFiles = recursivelyGetFrom JDrive remote folder
                // check the differences: actualRemoteFiles and RemoteToLocal.

                // if some difference has been found
                //     dispatch each difference through some queue...
                // else
                //     sleep for some time
                //}

                // FOR TESTING PURPOSE
                Thread.sleep(1000 * 60);
            }catch (IOException ioex ){
                log.error(ioex.getMessage()+". Exit.");
            }catch ( InterruptedException ignored ){
            }finally {
                log.info("RemoteWatcher closed.");
                if( dispatchingQueue != null ){
                    try {
                        dispatchingQueue.put( new Error("Remote Watcher is closing.") );
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }, "RemoteWatcher");

    /**
     * TODO add doc
     * @return
     */
    public static RemoteWatcher getInstance() {
        if( instance == null )
            instance = new RemoteWatcher();
        return instance;
    }

    private RemoteWatcher() {}

    public Mapper init() throws GeneralSecurityException, IOException {
        driveService = GoogleAuthenticator.getInstance().getDriveService();
        driveFileManager = DriveFileManager.getInstance();

        Path jdriveLocalPath = Paths.get(
            (String) Configurator.getInstance().get( Keys.WATCHED_BASE_PATH )
        );
        File jdriveRemoteFolder = getJDriveRemoteFolderOrCreate( jdriveLocalPath );
        log.info("JDrive remote folder found.");

        log.info("Starting mapping remote files...");
        Mapper mapper = new Mapper();
        recursivelyListFullPathsFrom(
            jdriveRemoteFolder,
            jdriveRemoteFolder.getName()
        ).forEach( mapper::put );
        log.info("Mapping remote file complete.");

        return mapper;
    }

    private File getJDriveRemoteFolderOrCreate( Path jdriveLocalBasePath )
            throws IOException {
        String query = String.format(
            "mimeType = '%s' and not trashed and 'root' in parents and name = '%s'",
            MIMEType.Remote.FOLDER, jdriveLocalBasePath.getFileName()
        );
        List<File> result = doQuery( query );

        if( result.size() > 1 )
            throw new IOException( "Multiple JDrive remote folder found." );

        if( result.isEmpty() ){
            result.add( driveFileManager.createRemoteFolderFrom(
                jdriveLocalBasePath
            ));
        }
        return result.get(0);
    }

    private HashMap<String, File> recursivelyListFullPathsFrom( File remoteFolder,
                                                                String partialPath )
            throws IOException {
        HashMap<String, File> pathsMap = new HashMap<>();
        pathsMap.put( partialPath, remoteFolder );

        if( remoteFolder.getMimeType().equals(MIMEType.Remote.FOLDER.toString()) ) {
            String q = String.format("not trashed and '%s' in parents", remoteFolder.getId());
            doQuery(q).forEach(
                file -> {
                    try {
                        pathsMap.putAll(
                            recursivelyListFullPathsFrom(
                                file, partialPath.concat("/"+file.getName())
                            )
                        );
                    } catch (IOException e) { log.error(e.getMessage(), e); }
                }
            );
        }
        return pathsMap;
    }

    private List<File> doQuery( String query ) throws IOException {
        Drive.Files.List request = driveService.files().list()
            .setQ( query )
            .setFields("files(id,name,parents,mimeType,kind,size,modifiedTime,trashed)")
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

    public void setDispatchingQueue( LinkedBlockingQueue<Event> queue ){
        this.dispatchingQueue = queue;
    }

    public void start(){
        this.thread.start();
    }

    public void startClosingProcedure(){
        log.info("RemoteWatcher closing procedure initialized...");
        this.thread.interrupt();
    }
}
