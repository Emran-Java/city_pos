package acquire.base.utils.file;

import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Files Utils
 *
 * @author Janson
 * @date 2022/7/18 14:29
 */
public class FileUtils {
    /**
     * Read file
     *
     * @param file the file to be read
     * @return file data
     */
    public static byte[] read(File file) {

        byte[] tempBuf = new byte[100];
        int byteRead;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            InputStream inputStream;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                inputStream = Files.newInputStream(file.toPath());
            }else{
                inputStream = new FileInputStream(file);
            }
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            while ((byteRead = bufferedInputStream.read(tempBuf)) != -1) {
                byteArrayOutputStream.write(tempBuf, 0, byteRead);
            }
            bufferedInputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Read file
     *
     * @param fileName the file name
     * @return file data
     */
    public static byte[] read(String fileName) {
        return read(new File(fileName));
    }

    /**
     * Read file chars
     *
     * @param file the file to be read
     * @return file char arrays
     */
    public static char[] readChars(File file) {
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        char[] tempBuf = new char[100];
        int charRead;

        try {
            InputStream inputStream;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                inputStream = Files.newInputStream(file.toPath());
            }else{
                inputStream = new FileInputStream(file);
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((charRead = bufferedReader.read(tempBuf)) != -1) {
                charArrayWriter.write(tempBuf, 0, charRead);
            }
            bufferedReader.close();
            return charArrayWriter.toCharArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Read file by lines
     *
     * @param file the file to be read
     * @return file text
     */
    public static String readLines(File file) {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            InputStream inputStream;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                inputStream = Files.newInputStream(file.toPath());
            }else{
                inputStream = new FileInputStream(file);
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String tempString;
            while ((tempString = bufferedReader.readLine()) != null) {
                stringBuilder.append(tempString).append("\n");
            }
            bufferedReader.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Write data to a file
     *
     * @param pos  the start position of file
     * @param bs   data to be written
     * @param path file path
     */
    public static void write(int pos, byte[] bs, String path) {

        if (new File(path).exists() && pos == 0) {
            new File(path).delete();
        }
        try {
            RandomAccessFile raff = new RandomAccessFile(path, "rw");
            raff.seek(pos);
            raff.write(bs);
            raff.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write data to a file
     *
     * @param file   the file to be written
     * @param data   data to be written
     * @param append true to write bytes in the end of the file.
     */
    public static void write(File file, byte[] data, boolean append) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file, append));
            bufferedOutputStream.write(data);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write char to a file
     *
     * @param file  the file to be written
     * @param chars chars to be written
     */
    public static void writeChars(File file, char[] chars) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            bufferedWriter.write(chars);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write text to a file
     *
     * @param file   the file to be written
     * @param str    string to be written
     * @param append true to write the string in the end of the file.
     */
    public static void writeString(File file, String str, boolean append) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append)));
            bufferedWriter.write(str);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete file
     *
     * @param file  the file to delete. It can be a file or dir.
     * @param isAll true if the file and its internal files are deleted; else, only the internal files are deleted.
     */
    public static void delete(File file, boolean isAll) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                if (isAll) {
                    file.delete();
                }
                return;
            }
            for (File f : childFile) {
                delete(f, true);
            }
            if (isAll) {
                file.delete();
            }
        }
    }

    /**
     * Get file size
     */
    public static long getFileTotalSize(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                long size = 0;
                if (children != null){
                    for (File f : children) {
                        size += getFileTotalSize(f);
                    }
                }
                return size;
            } else {
                return file.length();
            }
        }
        return 0;
    }

    /**
     * Creates a directory if not exist
     *
     * @param dir dir path
     * @return the directory file
     */
    public static File createDir(String dir) {
        System.out.println("createDir: " + dir);
        if (TextUtils.isEmpty(dir)) {
            return null;
        }
        try {
            File file = new File(dir);
            if (!file.exists()) {
                if (file.mkdirs()) {
                    System.out.println("create dir success: " + dir);
                    return file;
                }
            } else {
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create file.If its dir doesn't exist,create its dir.
     *
     * @param filePath file path
     * @return File
     */
    public static File createFile(String filePath) {
        File file = new File(filePath);
        if (createDir(file.getParent()) == null){
            return null;
        }
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Copy a dir include its child files
     *
     * @param srcDir source dir
     * @param dstDir destination file
     */
    public static boolean copyDir(File srcDir, File dstDir) {
        if (srcDir == null || !srcDir.exists() || !srcDir.canRead() || dstDir == null) {
            return false;
        }
        try {
            if (!dstDir.exists() && !dstDir.mkdirs()) {
                return false;
            }
            String dstPath = dstDir.getPath() + "/";
            File[] files = srcDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        File dstFile = new File(dstPath + file.getName());
                        if (!copyFile(file, dstFile)) {
                            return false;
                        }
                    } else if (file.isDirectory()) {
                        File dstFile = new File(dstPath + file.getName());
                        if (!copyDir(file, dstFile)) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Copy a file
     *
     * @param src source file
     * @param dst destination file
     */
    public static boolean copyFile(File src, File dst) {
        if (src == null || !src.exists() || !src.canRead() || dst == null) {
            return false;
        }
        if (dst.exists()) {
            dst.delete();
        }
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            dst.createNewFile();
            if (!dst.getParentFile().exists() && !dst.getParentFile().mkdirs()) {
                return false;
            }
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            int len;
            byte[] buf = new byte[4096];
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Copy a file
     *
     * @param srcIs  the {@link InputStream} of source file
     * @param dst destination file
     */
    public static boolean copyFileByInputStream(InputStream srcIs, File dst) {
        if (srcIs == null || dst == null) {
            return false;
        }
        try {
            if (dst.exists()) {
                dst.delete();
            }
            dst.createNewFile();
            if (!dst.getParentFile().exists() && !dst.getParentFile().mkdirs()) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try(FileOutputStream out = new FileOutputStream(dst)) {
            int len;
            byte[] buf = new byte[4096];
            while ((len = srcIs.read(buf)) != -1) {
                out.write(buf, 0, len);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Clear dir include its files
     *
     * @param dir  the dir to be cleared
     */
    public static boolean clearDir(File dir) {
        if (dir == null || !dir.exists()) {
            return true;
        }
        if (!dir.isDirectory() || !dir.canRead()) {
            return false;
        }
        try {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (!file.delete()) {
                            return false;
                        }
                    } else if (file.isDirectory()) {
                        if (!clearDir(file)) {
                            return false;
                        }
                        if (!file.delete()) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * unzip file
     *
     * @param srcZipPath source zip path
     * @param dstDirPath destination dir
     * @throws IOException IO error
     */
    public static void upZip(String srcZipPath, String dstDirPath) throws IOException {
        File destDir = new File(dstDirPath);
        if (destDir.exists()) {
            destDir.delete();
        }

        destDir.mkdirs();
        FileInputStream fins = new FileInputStream(srcZipPath);
        ZipInputStream zins = new ZipInputStream(fins);
        ZipEntry ze;
        byte[] ch = new byte[8192];
        while ((ze = zins.getNextEntry()) != null) {
            File zfile = new File(dstDirPath, ze.getName());
            File fpath = new File(zfile.getParentFile().getPath());
            if (ze.isDirectory()) {
                if (!zfile.exists()) {
                    zfile.mkdirs();
                }
                zins.closeEntry();
            } else {
                if (!fpath.exists()) {
                    fpath.mkdirs();
                }
                FileOutputStream fouts = new FileOutputStream(zfile);
                int i;
                while ((i = zins.read(ch)) != -1) {
                    fouts.write(ch, 0, i);
                }
                zins.closeEntry();
                fouts.close();
            }
        }
        fins.close();
        zins.close();
    }

    /**
     * Rename file
     *
     * @param oldFile source file
     * @param newName new name
     */
    public static boolean rename(File oldFile, String newName) {
        if (!oldFile.getName().equals(newName)) {
            if (!oldFile.exists()) {
                return false;
            }
            File newfile = new File(oldFile.getParent(), newName);
            if (newfile.exists()) {
                newfile.delete();
            }
            return oldFile.renameTo(newfile);

        }
        return true;
    }

}
