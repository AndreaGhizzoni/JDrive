package it.hackcaffebabe.jdrive.remote.google;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * This class check and log the status of every upload and download
 */
class FileProgressListener implements MediaHttpUploaderProgressListener,
                                      MediaHttpDownloaderProgressListener {

    private static final Logger log = LogManager.getLogger();

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        if (uploader == null) return;
        switch (uploader.getUploadState()) {
            case INITIATION_STARTED:
                log.debug("Upload initialization started");
                break;
            case INITIATION_COMPLETE:
                log.debug("Upload initialization complete");
                break;
            case MEDIA_IN_PROGRESS:
                log.debug("Upload: "+
                    formatProgress(
                        uploader.getNumBytesUploaded(),
                        uploader.getProgress()
                    )
                );
                break;
            case MEDIA_COMPLETE:
                log.debug("Upload complete");
                break;
        }
    }

    @Override
    public void progressChanged(MediaHttpDownloader downloader) throws IOException {
        if (downloader == null) return;
        switch (downloader.getDownloadState()){
            case NOT_STARTED:
                log.debug("Download not stared");
                break;
            case MEDIA_IN_PROGRESS:
                log.debug("Download: "+
                    formatProgress(
                        downloader.getNumBytesDownloaded(),
                        downloader.getProgress()
                    )
                );
                break;
            case MEDIA_COMPLETE:
                log.debug("Download complete");
                break;
        }
    }

    private String formatProgress( long bytes, double progress ){
        return  String.valueOf( bytes ) + " bytes | ~" +
                formatPercentage( progress );
    }

    private String formatPercentage( double percentage ){
        return new DecimalFormat("#00.00").format( percentage*100 ) +" %";
    }
}
