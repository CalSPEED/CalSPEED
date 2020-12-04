/*
Copyright (c) 2020, California State University Monterey Bay (CSUMB).
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. Neither the name of the CPUC, CSU Monterey Bay, nor the names of
       its contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package gov.ca.cpuc.calspeed.android;

import gov.ca.cpuc.calspeed.android.AndroidUiServices.TextOutputAdapter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

class SaveResults {
    private final String filename;
    private final TextOutputAdapter display;
    private final TextOutputAdapter summary;
    private final String displayResults;
    private final Date date;
    private String errorMessage = "";

    SaveResults(TextOutputAdapter display, TextOutputAdapter summary, String displayResults,
                Date date) throws InterruptedException {
        this.displayResults = displayResults;
        this.display = display;
        this.summary = summary;
        this.date = date;
        this.filename = getFileName();
    }

    private String getFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyykkmmssSSS", Locale.US);
        return (String.format("%s.txt", sdf.format(date)));
    }

    private String[] getAllResultsFiles() {
        String[] resultsFiles;
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString() + "/CPUC/";
        try {
            File sdcardDirectory = new File(extStorageDirectory);
            //create a FilenameFilter and override its accept-method
            FilenameFilter filefilter = new FilenameFilter() {
                //Mini-function to determine whether or not a file is a CPUC tester file
                public boolean accept(File dir, String name) {
                    //If the file extension is .txt return true, else false
                    return name.endsWith(".txt");
                }
            };
            resultsFiles = sdcardDirectory.list(filefilter);
            return resultsFiles;
        } catch (Exception e) {
            errorMessage += "Error: Unable to get results file names, " + extStorageDirectory +
                    " is not a valid directory. \n";
            Log.e(getClass().getName(), "Error: Unable to get results file names, " +
                    extStorageDirectory + " is not a valid directory. \n");
            return null;
        }
    }

    Boolean uploadAllFiles(TextOutputAdapter summary, TextOutputAdapter results)
            throws InterruptedException {
        String[] resultsFiles = getAllResultsFiles();
        if ((resultsFiles == null) || (resultsFiles.length == 0)) {
            summary.append("No Files Found.\n");
            results.append("No Files Found.\n");
        } else {
            String sdcardDir = Environment.getExternalStorageDirectory().toString();
            String uploadDirName = sdcardDir + "/Uploaded";
            File uploadDirectory = new File(uploadDirName);
            if (!uploadDirectory.exists()) {
                if (uploadDirectory.mkdir()) {
                    Log.d(getClass().getName(), "Upload directory created");
                }
            }
            int i;
            String status;
            results.append("Uploading " + resultsFiles.length + " files...\n");
            summary.append("Uploading " + resultsFiles.length + " files...\n");
            for (i = 0; i < resultsFiles.length; i++) {
                Log.v("debug", "Filename: " + resultsFiles[i] + "\n");
                status = saveResultsToServer(resultsFiles[i]);
                results.append(status);
                if (status.contains("Error") || status.contains("error")) {
                    errorMessage += "Error: Aborting upload on file #" + i + " " + resultsFiles[i] + " Could not upload files to server.\n";
                    Log.v("debug", "Error: Aborting upload on file #" + i + " " + resultsFiles[i] + " Could not upload files to server.\n");
                    results.append("\nAborting Upload--on " + resultsFiles[i] + "\n  Error uploading files to server.\n");
                    return (false);
                } else {
                    Integer fileCount = i + 1;
                    summary.append("File " + fileCount + " uploaded.\n");
                    File f = new File(sdcardDir + "/CPUC/" + resultsFiles[i]);
                    if (f.delete()) {
                        Log.d(getClass().getName(), resultsFiles[i] + " file deleted");
                    }
                }
            }
            for (File file : uploadDirectory.listFiles()) {
                if (file.delete()) {
                    Log.d(getClass().getName(), file.getName() + " file deleted");
                }
            }
            if (uploadDirectory.delete())  {
                Log.d(getClass().getName(), "Upload directory deleted");
            }
        }
        return (true);
    }

    private String writeText(String text) {
        if (display != null) {
            display.append(text);
        }
        if (summary != null) {
            summary.append(text);
        }
        text = "";
        return (text);
    }

    String saveResultsLocally() {
        if (sDCardIsWritable()) {
            try {
                String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                extStorageDirectory += "/CPUC";
                if (!makeSureDirExists(extStorageDirectory)) {
                    return "Error: Could not find or create CPUC directory \n";
                }
                File testFile = new File(extStorageDirectory, filename);
                FileOutputStream outStream = new FileOutputStream(testFile);
                if (testFile.createNewFile()) {
                    Log.d(getClass().getName(), "Test file " + filename + " created");
                }
                outStream.write(displayResults.getBytes());
                outStream.close();
                return ("File " + filename + " successfully saved to sdcard.\n");
            } catch (Exception e) {
                errorMessage = "Error: Unable to write results file to sdcard.\n";
                Log.e(getClass().getName(), "Error: Unable to write results file to sdcard.\n");
                return ("Error in saving file " + filename + " to sdcard--file not saved.\n");
            }
        } else {
            return ("SD Card not writable. Check SD card status.\n");
        }
    }


    private boolean sDCardIsWritable() {
        boolean mExternalStorageWriteable;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageWriteable = false;
        }
        return mExternalStorageWriteable;
    }

    private String saveResultsToServer(String filename) {
        try {
            String message = "";
            if (!filename.matches("")) {
                SecureFileTransfer sf = new SecureFileTransfer(
                        filename,
                        Environment.getExternalStorageDirectory().toString() + "/CPUC/",
                        "./UploadData/");
                message = sf.send();
            }
            return (message + "\n");
        } catch (Exception e) {
            errorMessage += "Error: Unable to upload file to server.\n";
            Log.e(getClass().getName(), "Unable to upload file to server.\n");
            return ("Error uploading file " + filename + " to server--file not uploaded.\n");
        }
    }

    /**
     * Checks if a directory exists. If it doesn't, creates it. Returns true
     * if the directory exists or is successfully created, otherwise returns
     * false.
     */
    private Boolean makeSureDirExists(String filenameToTest) {
        File checkExist = new File(filenameToTest);
        try {
            if (!checkExist.exists()) {
                boolean success = checkExist.mkdir();
                if (!success) {
                    errorMessage += "Error: Could not create directory " + filenameToTest;
                    Log.e(getClass().getName(), "Error: Could not create directory " + filenameToTest);
                    return false;
                }
            }
        } catch (Exception e) {
            errorMessage += "Error: Could not create directory " + filenameToTest;
            Log.e(getClass().getName(), "Error: Could not create directory " + filenameToTest);
            return false;
        }
        return true;
    }

    String getErrorMessage() {
        return errorMessage;
    }

    void clearErrorMessage() {
        errorMessage = "";
    }
}

