package com.example.googlemapexample;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;

class FileLog {
    private String mFileName;
    private String mLogTag;
    private Context mContext;

    FileLog(Context context, String fileName, String logTag) {
        this.mFileName = fileName;
        this.mLogTag = logTag;
        this.mContext = context;
    }

    void logString(String logString) {
        final String ts = (new Date()).toString();

        Log.d(mLogTag, logString);
        storeRecordInFile(mFileName, logString + " " + ts);
    }

    void logLocation(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final String ts = (new Date()).toString();
        Log.d(mLogTag, "Current location is" + location.toString());
        Date location_measurement_ts = new Date(location.getTime());
        storeRecordInFile(mFileName, String.format(Locale.US,
                "{\"utcTime\":\"%s\", \"measurementUtcTime\":\"%s\", " +
                        "\"type\":\"updated\", \"provider\":\"%s\", \"accuracy\":%f, " +
                        "\"latitude\":%f, \"longitude\":%f},",
                ts, location_measurement_ts.toString(), location.getProvider(),
                location.getAccuracy(), latitude, longitude));

        storeRecordInFile(mFileName, String.format(Locale.US,
                "https://www.google.com/maps/search/?api=1&query=%f,%f",
                latitude, longitude));

        Log.d(mLogTag, String.format(Locale.US,
                "Received location: Latitude:%f, Longitude:%f", latitude, longitude));
        Log.d(mLogTag, String.format(Locale.US,
                "Location link: https://www.google.com/maps/search/?api=1&query=%f,%f",
                latitude, longitude));
    }

    private boolean storeRecordInFile(String filename, String msg) {
        File m_recordFile = prepareLogFile(filename);
        if (m_recordFile != null) {
            try {
                FileOutputStream f = new FileOutputStream(m_recordFile, true);
                PrintWriter pw = new PrintWriter(f);
                pw.println(msg);
                pw.close();
                f.close();
            } catch (FileNotFoundException ex) {
                Log.e(mLogTag, "Failed to open the file. " + ex);
                return false;
            } catch (IOException ex) {
                Log.e(mLogTag, "Failed to write the data into the file. " + ex);
                return false;
            }
            return true;
        } else {
            Log.e(mLogTag, "File for storing the data doesn't exist");
            return false;
        }
    }

    private File prepareLogFile(String filename) {
        if (isExternalStorageWritable()) {
            File dir = new File(Environment.getExternalStorageDirectory() +
                    "/Android/data/" + mContext.getApplicationContext().getPackageName());
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(mLogTag, "Failed to create directory " + dir.getAbsolutePath());
                    return null;
                }
            }
            return new File(dir, filename);
        } else {
            Log.e(mLogTag, "External storage is not writable. Exiting application");
            return null;
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }
}
