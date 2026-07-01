package acquire.app.brac.utility;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileUtility {

    public static final String EMPTY_FILE_TYPE = "no_extension";
    public static final String FILE_TYPE_INI = "ini";
    public static final String FILE_TYPE_MP4 = "mp4";
    public static final String FILE_TYPE_JPG = "jpg";
    public static final String FILE_TYPE_JPEG = "jpeg";
    public static final String FILE_TYPE_PNG = "png";
    public static final String FILE_TYPE_GIF = "gif";

    public static ArrayList<String> acceptableMediaFiles = new ArrayList<>(Arrays.asList(FILE_TYPE_MP4, FILE_TYPE_JPG, FILE_TYPE_JPEG, FILE_TYPE_PNG, FILE_TYPE_GIF));
    private static FileUtility mFileUtility;

    private OnFilesCountListener onFilesCountListener;

    public static FileUtility getInstance() {
        if (mFileUtility == null) {
            mFileUtility = new FileUtility();
        }
        return mFileUtility;
    }

    public void setOnFileCountFinishedListener(OnFilesCountListener onFilesCountListener) {
        this.onFilesCountListener = onFilesCountListener;
    }

/*    public void loadImageFromFileUrl(Context context, ImageView imageView, String path) {
        File file = new File(path);

        if (!file.exists()) return;
        String extension = getFileExtension(path);

        if (extension.equalsIgnoreCase("mp4")) {
            return;
        }

        Glide.with(context).asGif().load(file).into(imageView);
    }*/
public void loadImageFromFileUrl(Context context, ImageView imageView, String path) {

    if (path == null || path.trim().isEmpty()) {
        return;
    }

    File file = new File(path);

    if (!file.exists()) {
        return;
    }

    String extension = getFileExtension(path);

    if (extension != null && extension.equalsIgnoreCase("mp4")) {
        return;
    }

    Glide.with(context)
            .load(file)
            .into(imageView);
}

    public void countFiles(String directoryPath) {
        //this.onFilesCountListener = onFilesCountListener;
        File dir = new File(directoryPath);

        if (!dir.exists() || !dir.isDirectory()) {
            Log.d("FileCounter", "Invalid directory");
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) return;

        int totalFiles = 0;
        Map<String, Integer> fileTypeMap = new HashMap<>();
        ArrayList<FileCountModel> filesPath = new ArrayList<>();

        for (File file : files) {
            if (file.isFile()) {
                totalFiles++;

                Log.d("FileCounter", " getAbsolutePath: " + file.getAbsolutePath());
                Log.d("FileCounter", " getAbsoluteFile: " + file.getAbsoluteFile());
                Log.d("FileCounter", " getPath: " + file.getPath());
                String extension = getFileExtension(file.getName());


                if (extension.isEmpty()) {
                    extension = EMPTY_FILE_TYPE;
                }

                filesPath.add(new FileCountModel(file.getPath(), file.getName(), extension));

                fileTypeMap.put(
                        extension,
                        fileTypeMap.getOrDefault(extension, 0) + 1
                );
            }
        }

        try {
            this.onFilesCountListener.filesCountCompleted(filesPath);
        } catch (Exception ex) {
            Log.d("FileCounter", "this.onFilesCountListener.filesCountCompleted(filesPath); exception : " + ex.getMessage());
        }

        Log.d("FileCounter", "Total Files: " + totalFiles);

        for (Map.Entry<String, Integer> entry : fileTypeMap.entrySet()) {
            Log.d("FileCounter", entry.getKey() + " : " + entry.getValue());
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return "";
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    public interface OnFilesCountListener {
        void filesCountCompleted(ArrayList<FileCountModel> files);
    }

    public static class FileCountModel {
        String filePath;
        String fileName;
        String fileType;

        public FileCountModel(String filePath, String fileName, String fileType) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.fileType = fileType;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        @Override
        public String toString() {
            return "FileCountModel{" +
                    "filePath='" + filePath + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", fileType='" + fileType + '\'' +
                    '}';
        }
    }
}
