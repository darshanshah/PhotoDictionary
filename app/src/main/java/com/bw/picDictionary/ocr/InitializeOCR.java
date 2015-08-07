package com.bw.picDictionary.ocr;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Start working with OCR library , initialize the OCR to recognize
 */
final class InitializeOCR extends AsyncTask<String, String, Boolean> {
    private static final String TAG = InitializeOCR.class.getSimpleName();

    private static final String[] CUBE_DATA_FILES = {
            "eng.traineddata",
            "wiktionary",
            "Types",
            "primary-index",
            "secondary-index"
    };
    private final String languageCode;
    private ImageCaptureActivity activity;
    private Context context;
    private TessBaseAPI baseApi;
    private ProgressDialog dialog;
    private ProgressDialog indeterminateDialog;
    private String languageName;
    private int ocrEngineMode;


    InitializeOCR(ImageCaptureActivity activity, TessBaseAPI baseApi, ProgressDialog dialog,
                  ProgressDialog indeterminateDialog, String languageCode, String languageName,
                  int ocrEngineMode) {
        this.activity = activity;
        this.context = activity.getBaseContext();
        this.baseApi = baseApi;
        this.dialog = dialog;
        this.indeterminateDialog = indeterminateDialog;
        this.languageCode = languageCode;
        this.languageName = languageName;
        this.ocrEngineMode = ocrEngineMode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setTitle("Please wait");
        dialog.setMessage("Checking for data installation...");
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.show();
        activity.setButtonVisibility(false);
    }

    /**
     *  // Check whether we need Cube data or Tesseract data.
     // Example Cube data filename: "tesseract-ocr-3.01.eng.tar"
     // http://ocr-dictionary.googlecode.com/files/dictionary_data.tar.gz
     // Example Tesseract data filename: "eng.traineddata"
     *
     * @param params [0] path for the directory to String langauge
     */
    protected Boolean doInBackground(String... params) {

        String destinationFilenameBase = languageCode + ".traineddata";
        boolean isCubeSupported = false;
        for (String s : ImageCaptureActivity.CUBE_SUPPORTED_LANGUAGES) {
            if (s.equals(languageCode)) {
                isCubeSupported = true;
                destinationFilenameBase = "dictionary_data.tar";
            }
        }


        String destinationDirBase = params[0];
        File tessdataDir = new File(destinationDirBase + File.separator + "tessdata");
        if (!tessdataDir.exists() && !tessdataDir.mkdirs()) {
            Log.e(TAG, "Couldn't make directory " + tessdataDir);
            return false;
        }
        File downloadFile = new File(tessdataDir, destinationFilenameBase);
        File incomplete = new File(tessdataDir, destinationFilenameBase + ".download");
        if (incomplete.exists()) {
            incomplete.delete();

        }
        boolean isAllCubeDataInstalled = false;
        if (isCubeSupported) {
            boolean isAFileMissing = false;
            File dataFile;
            for (String s : CUBE_DATA_FILES) {
                dataFile = new File(tessdataDir.toString() + File.separator + s);
                if (!dataFile.exists()) {
                    isAFileMissing = true;
                }
            }
            isAllCubeDataInstalled = !isAFileMissing;
        }

        boolean installSuccess = false;
        boolean download = false;

        if (!isAllCubeDataInstalled) {
            Log.d(TAG, "Language data for " + languageCode + " not found in " + tessdataDir.toString());
            deleteCubeDataFiles(tessdataDir);


            if (!installSuccess) {
                if (new File(tessdataDir.toString() + File.separator + destinationFilenameBase).exists()) {
                    try {
                        untar(tessdataDir.toString() + File.separator + destinationFilenameBase, new File(tessdataDir.toString() + File.separator + destinationFilenameBase), tessdataDir);
                        installSuccess = true;
                    } catch (IOException e) {
                        Log.e(TAG, "Untar failed");
                        return false;
                    }
                } else {
                    Log.d(TAG, "Downloading " + destinationFilenameBase + ".gz...");
                    try {
                        download = downloadFile(destinationFilenameBase, downloadFile);
                        if (!download) {
                            Log.e(TAG, "Download failed");
                            return false;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IOException received in doInBackground. Is a network connection available?");
                        return false;
                    }
                }
            }
            String extension = destinationFilenameBase.substring(
                    destinationFilenameBase.lastIndexOf('.'),
                    destinationFilenameBase.length());

            if (extension.equals(".tar") && !installSuccess) {
                try {
                    untar(tessdataDir.toString() + File.separator + destinationFilenameBase, new File(tessdataDir.toString() + File.separator + destinationFilenameBase), tessdataDir);
                    installSuccess = true;
                } catch (IOException e) {
                    Log.e(TAG, "Untar failed");
                    return false;
                }
            }


        } else {
            Log.d(TAG, "Language data for " + languageCode + " already installed in "
                    + tessdataDir.toString());
            installSuccess = true;
        }


        dialog.dismiss();

        if (baseApi.init(destinationDirBase + File.separator, languageCode, ocrEngineMode)) {
            return installSuccess;
        }
        return false;
    }

    /**
     * Remove any data files for the cube in the directory.
     *
     * @param tessdataDir Directory to delete the files from
     */
    private void deleteCubeDataFiles(File tessdataDir) {
        File badFile;
        for (String s : CUBE_DATA_FILES) {
            badFile = new File(tessdataDir.toString() + File.separator + languageCode + s);
            if (badFile.exists()) {
                Log.d(TAG, "Deleting existing file " + badFile.toString());
                badFile.delete();
            }
            badFile = new File(tessdataDir.toString() + File.separator + "tesseract-ocr-3.01."
                    + languageCode + ".tar");
            if (badFile.exists()) {
                Log.d(TAG, "Deleting existing file " + badFile.toString());
                badFile.delete();
            }
        }
    }

    /**
     * Download a file from site specifile in the base
     * @param sourceFilenameBase Name of file to download, minus the required ".gz" extension
     * @param destinationFile    Name of file to save the unzipped data to, including path
     * @return True if download and unzip are successful
     * @throws IOException
     */
    private boolean downloadFile(String sourceFilenameBase, File destinationFile)
            throws IOException {
        try {
            return downloadGzippedFileHttp(new URL(ImageCaptureActivity.DOWNLOAD_BASE + sourceFilenameBase + ".gz"), destinationFile);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Bad URL string.");
        }
    }

    /**
     * get zip data and unzip it to the destination file paramter location
     * @param url             URL to download from
     * @param destinationFile File to save the download as, including path
     * @return True if response received, destinationFile opened, and unzip
     * successful
     * @throws IOException
     */
    private boolean downloadGzippedFileHttp(URL url, File destinationFile)
            throws IOException {
        // Send an HTTP GET request for the file
        Log.d(TAG, "Sending GET request to " + url + "...");
        publishProgress("Downloading data for Dictionary...", "0");
        HttpURLConnection urlConnection = null;
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setAllowUserInteraction(false);
        urlConnection.setInstanceFollowRedirects(true);
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            Log.e(TAG, "Did not get HTTP_OK response.");
            Log.e(TAG, "Response code: " + urlConnection.getResponseCode());
            Log.e(TAG, "Response message: " + urlConnection.getResponseMessage().toString());
            return false;
        }
        int fileSize = urlConnection.getContentLength();
        InputStream inputStream = urlConnection.getInputStream();
        File tempFile = new File(destinationFile.toString() + ".gz.download");

        Log.d(TAG, "Streaming download to " + destinationFile.toString() + ".gz.download...");
        final int BUFFER = 8192;
        FileOutputStream fileOutputStream = null;
        Integer percentComplete;
        int percentCompleteLast = 0;
        try {
            fileOutputStream = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception received when opening FileOutputStream.", e);
        }
        int downloaded = 0;
        byte[] buffer = new byte[BUFFER];
        int bufferLength = 0;
        while ((bufferLength = inputStream.read(buffer, 0, BUFFER)) > 0) {
            fileOutputStream.write(buffer, 0, bufferLength);
            downloaded += bufferLength;
            percentComplete = (int) ((downloaded / (float) fileSize) * 100);
            if (percentComplete > percentCompleteLast) {
                publishProgress("Downloading data for Dictionary...",
                        percentComplete.toString());
                percentCompleteLast = percentComplete;
            }
        }
        fileOutputStream.close();
        if (urlConnection != null) {
            urlConnection.disconnect();
        }

        try {
            Log.d(TAG, "Unzipping...");
            gunzip(tempFile, new File(tempFile.toString().replace(".gz.download", "")));
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not available for unzipping.");
        } catch (IOException e) {
            Log.e(TAG, "Problem unzipping file.");
        }
        return false;
    }

    /**
     * get zip data and unzip it to the destination file paramter location and delete the zip file.
     */
    private void gunzip(File zippedFile, File outFilePath) throws FileNotFoundException, IOException {
        int uncompressedFileSize = getGzipSizeUncompressed(zippedFile);
        Integer percentComplete;
        int percentCompleteLast = 0;
        int unzippedBytes = 0;
        final Integer progressMin = 0;
        int progressMax = 100 - progressMin;
        publishProgress("Uncompressing data for Dictionary...", progressMin.toString());

        // If the file is a tar file, just show progress to 50%
        String extension = zippedFile.toString().substring(zippedFile.toString().length() - 16);
        if (extension.equals(".tar.gz.download")) {
            progressMax = 50;
        }
        GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(zippedFile)));
        OutputStream outputStream = new FileOutputStream(outFilePath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        final int BUFFER = 8192;
        byte[] data = new byte[BUFFER];
        int len;
        while ((len = gzipInputStream.read(data, 0, BUFFER)) > 0) {
            bufferedOutputStream.write(data, 0, len);
            unzippedBytes += len;
            percentComplete = (int) ((unzippedBytes / (float) uncompressedFileSize) * progressMax) + progressMin;

            if (percentComplete > percentCompleteLast) {
                publishProgress("Uncompressing data for Dictionary...", percentComplete.toString());
                percentCompleteLast = percentComplete;
            }
        }
        gzipInputStream.close();
        bufferedOutputStream.flush();
        bufferedOutputStream.close();

        if (zippedFile.exists()) {
            zippedFile.delete();
        }
    }


    private int getGzipSizeUncompressed(File zipFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(zipFile, "r");
        raf.seek(raf.length() - 4);
        int b4 = raf.read();
        int b3 = raf.read();
        int b2 = raf.read();
        int b1 = raf.read();
        raf.close();
        return (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
    }

    /**
     * Support for tar file similar to zip file...
     * Uses jtar: http://code.google.com/p/jtar/
     *
     */
    private void untar(String path, File tarFile, File destinationDir) throws IOException {
        Log.d(TAG, "Untarring...");
        final int uncompressedSize = getTarSizeUncompressed(tarFile);
        Integer percentComplete;
        int percentCompleteLast = 0;
        int unzippedBytes = 0;
        final Integer progressMin = 50;
        final int progressMax = 100 - progressMin;
        publishProgress("Uncompressing data for Dictionary...", progressMin.toString());

        //New Code...
        TarEntry entry;
        TarInputStream inputStream = null;
        FileOutputStream outputStream = null;

        //File tarFile = new File(tarFile);

        try {
            // get a stream to tar file
            inputStream = new TarInputStream(new FileInputStream(tarFile));


            // ADD LOOP HERE (SEE NEXT STEP)
            // For each entry in the tar, extract and save the entry to the file system
            while (null != (entry = inputStream.getNextEntry())) {
                // for each entry to be extracted
                int bytesRead;
                String pathWithoutName = path.substring(0, path.indexOf(tarFile.getName()));

                // if the entry is a directory, create the directory
                if (entry.isDirectory()) {
                    File fileOrDir = new File(pathWithoutName + entry.getName());
                    fileOrDir.mkdir();
                    continue;
                }

                // write to file
                byte[] buf = new byte[8192];
                System.out.println(entry.getName());
                outputStream = new FileOutputStream(pathWithoutName + entry.getName());
                while ((bytesRead = inputStream.read(buf, 0, 8192)) > -1) {
                    outputStream.write(buf, 0, bytesRead);
                    unzippedBytes += bytesRead;
                    percentComplete = (int) ((unzippedBytes / (float) uncompressedSize) * progressMax) + progressMin;
                    if (percentComplete > percentCompleteLast) {
                        publishProgress("Uncompressing data for Dictionary...",
                                percentComplete.toString());
                        percentCompleteLast = percentComplete;
                    }
                }
                try {
                    if (null != outputStream)
                        outputStream.close();
                } catch (Exception e) {
                }

                System.out.println("Extracted " + entry.getName());
            }// while


        } catch (Exception e) {
            e.printStackTrace();
            try {
                throw new Exception("An error occurred while extracting file. Error " + e.getMessage());
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } finally { // close your streams
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }

        if (tarFile.exists()) {
            tarFile.delete();
        }
    }


    private int getTarSizeUncompressed(File tarFile) throws IOException {
        int size = 0;
        TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(tarFile)));
        TarEntry entry;
        while ((entry = tis.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                size += entry.getSize();
            }
        }
        return size;
    }


    /**
     * Update the dialog box.
     *
     * @param message [0] Text to be displayed
     * @param message [1] Numeric value for the progress
     */
    @Override
    protected void onProgressUpdate(String... message) {
        super.onProgressUpdate(message);
        int percentComplete = 0;

        percentComplete = Integer.parseInt(message[1]);
        dialog.setMessage(message[0]);
        dialog.setProgress(percentComplete);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        indeterminateDialog.dismiss();
        ImageCaptureActivity.fileCheck = true;
        if (result) {
            activity.resumeOCR();
            activity.showLanguageName();
        } else {
            activity.showErrorMessage("Error", "Network is unreachable Please enable network access and restart this app.");
        }
    }
}