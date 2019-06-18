package com.yunding.ydbaselib.ydlog;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orhanobut.logger.LogStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.yunding.ydbaselib.ydlog.YDLogUtils.checkNotNull;

/**
 * Abstract class that takes care of background threading the file log operation on Android.
 * implementing classes are free to directly perform I/O operations there.
 *
 * Writes all logs to the disk with CSV format.
 */
public class YDDiskLogStrategy implements LogStrategy {

    @NonNull private final Handler handler;

    public YDDiskLogStrategy(@NonNull Handler handler) {
        checkNotNull(handler);
        this.handler = handler;
    }

    @Override
    public void log(int level, @Nullable String tag, @NonNull String message) {
        checkNotNull(message);
        // do nothing on the calling thread, simply pass the tag/msg to the background thread
        handler.sendMessage(handler.obtainMessage(level, message));
    }

    public static class WriteHandler extends Handler {

        @NonNull private final String folder;
        private final int maxFileSize;
        private final int maxFileCount;

        public WriteHandler(@NonNull Looper looper, @NonNull String folder, int maxFileSize, int maxFileCount) {
            super(looper);
            this.folder = folder;
            this.maxFileSize = maxFileSize;
            this.maxFileCount = maxFileCount;
        }

        @SuppressWarnings("checkstyle:emptyblock")
        @Override
        public void handleMessage(@NonNull Message msg) {
            String content = (String) msg.obj;

            FileWriter fileWriter = null;
            File logFile = getLogFile(folder, "logs");

            try {
                fileWriter = new FileWriter(logFile, true);

                writeLog(fileWriter, content);

                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                if (fileWriter != null) {
                    try {
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (IOException e1) { /* fail silently */ }
                }
            }
        }

        /**
         * This is always called on a single background thread.
         * Implementing classes must ONLY write to the fileWriter and nothing more.
         * The abstract class takes care of everything else including close the stream and catching IOException
         *
         * @param fileWriter an instance of FileWriter already initialised to the correct file
         */
        private void writeLog(@NonNull FileWriter fileWriter, @NonNull String content) throws IOException {
            checkNotNull(fileWriter);
            checkNotNull(content);

            fileWriter.append(content);
        }

        private File getLogFile(@NonNull String folderName, @NonNull String fileName) {
            checkNotNull(folderName);
            checkNotNull(fileName);

            File folder = new File(folderName);
            if (!folder.exists()) {
                //TODO: What if folder is not created, what happens then?
                folder.mkdirs();
            }

            List<File> files = YDLogUtils.getDirAllFile(folder);
            int exitFileCount = files.size();
            boolean shouldCreateFile = false;
            File lastFile = null;

            if (exitFileCount == 0) {
                shouldCreateFile = true;
            } else {
                lastFile = files.get(exitFileCount - 1);
                if (lastFile.length() >= maxFileSize) {
                    shouldCreateFile = true;
                }
            }

            if (shouldCreateFile) {
                long ts = System.currentTimeMillis();
                String tmpStr = String.format("%s-%s.log", fileName, String.valueOf(ts));
                String md5FileName= YDLogUtils.md5(tmpStr);
                File newFile = new File(folder, md5FileName);

                if (exitFileCount >= maxFileCount) {
                    File willRemoveFile = files.get(0);
                    if (willRemoveFile.exists()) {
                        willRemoveFile.delete();
                    }
                }
                return newFile;
            } else {
                return  lastFile;
            }
        }
    }
}
