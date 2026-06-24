package acquire.core.tools;



import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;

public class RecordExportUtil {

    private static final String TAG = "RecordExportUtil";

    public static String exportRecordsToJson(Context context) {
        try {
            // SQLite table data fetch
            RecordRepository repository = new RecordRepository();
            List<Record> recordList = repository.findAll();

            if (recordList == null || recordList.isEmpty()) {
                return "No data found";
            }

            // Convert to JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonData = gson.toJson(recordList);

            String fileName = "record_backup_" + System.currentTimeMillis() + ".json";

            // Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                OutputStream outputStream = context.getContentResolver()
                        .openOutputStream(
                                context.getContentResolver().insert(
                                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                        values
                                )
                        );

                if (outputStream != null) {
                    outputStream.write(jsonData.getBytes());
                    outputStream.flush();
                    outputStream.close();
                }

                return "Saved to Downloads/" + fileName;
            }
            // Android 9 and below
            else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                );

                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }

                File file = new File(downloadsDir, fileName);

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(jsonData.getBytes());
                fos.flush();
                fos.close();

                return "Saved to: " + file.getAbsolutePath();
            }

        } catch (Exception e) {
            Log.e(TAG, "Export failed", e);
            return "Export failed: " + e.getMessage();
        }
    }
}