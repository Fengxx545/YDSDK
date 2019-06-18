package com.yunding.ydbaselib.ydlog;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogcatLogStrategy;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.orhanobut.logger.Logger.DEBUG;
import static com.orhanobut.logger.Logger.ERROR;
import static com.orhanobut.logger.Logger.INFO;
import static com.orhanobut.logger.Logger.VERBOSE;
import static com.orhanobut.logger.Logger.WARN;

/**
 * Created by jzhao on 2016/4/20.
 */
public class YDLogger {
    private static boolean mLogFlag = false;
    private static final int MAX_BYTES = 300 * 1024; // 每个文件最多300k
    private static final int MAX_FILE_COUNT = 3; // 最多保持3个文件
    private static String TAG = "YDBleManager";
    private static final String FILENAME = "YDASDKLog";

    private static YDLogger mDingding = null;
    private String mLogFolderPath = null;
    private boolean mIsStarting = false;

    private YDLogger() {

    }

    public static YDLogger initLogger(String tag){
        if (mDingding == null) {
            mDingding = new YDLogger();
        }
        TAG = tag;
        return  mDingding;
    }

    /**
     * 通过设置tag获取logger实例
     *
     * @return logger实例
     */
    public static YDLogger ddLog(String tag) {
        if (mDingding == null) {
            mDingding = new YDLogger();
        }
        TAG = tag;
        return mDingding;
    }

    /**
     * 获取logger实例
     *
     * @return logger实例
     */
    public static YDLogger getLogger() {
        if (mDingding == null) {
            mDingding = new YDLogger();
        }
        return mDingding;
    }

    /**
    *  开启日志
    */
    public void start() {
        if (mIsStarting) {
            return;
        }

        mIsStarting = true;

        // 添加本地日志
        String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        // 日志文件路径
        mLogFolderPath = diskPath + File.separatorChar + FILENAME;

        HandlerThread ht = new HandlerThread("YDASDKFileLogger." + mLogFolderPath);
        ht.start();
        Handler handler = new YDDiskLogStrategy.WriteHandler(ht.getLooper(), mLogFolderPath, MAX_BYTES, MAX_FILE_COUNT);
        YDDiskLogStrategy diskLogStrategy = new YDDiskLogStrategy(handler);

        FormatStrategy diskFormatStrategy = YDDiskLogFormatStrategy.newBuilder()
                .logStrategy(diskLogStrategy)
                .tag(TAG)
                .build();
        Logger.addLogAdapter(new DiskLogAdapter(diskFormatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return mLogFlag;
            }
        });

        // 添加logcat日志
        LogcatLogStrategy logcatStrategy = new LogcatLogStrategy();
        FormatStrategy logCatFormatStrategy = YDLogCatFormatStrategy.newBuilder()
                .logStrategy(logcatStrategy)
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(logCatFormatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return mLogFlag;
            }
        });
    }

    /**
     *  关闭日志
     */
    public void stop() {
        mIsStarting = false;
        Logger.clearLogAdapters();
    }

    /**
     *  清除日志
     */
    public void clear() {
        // TODO 未针对用户
        stop();

        String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        // 日志文件路径
        String logFolderPath = diskPath + File.separatorChar + FILENAME;
        File folder = new File(logFolderPath);
        if (folder.exists()) {
            File[] fileArray = folder.listFiles();
            if (fileArray == null)
                return;
            for (File f : fileArray) {
                f.delete();
            }
        }
    }

    /**
     *  获取日志文件
     *
     *  @return 日志文件列表
     */
    public List<File> getLogFilePaths() {
        if (mLogFolderPath != null) {
            File folder = new File(mLogFolderPath);
            if (folder.exists()) {
                File[] fileArray = folder.listFiles();
                if (fileArray == null)
                    return null;
                List<File> fileList = new ArrayList<>();
                for (File f : fileArray) {
                    fileList.add(f);
                }
                return fileList;
            }
        }
        return null;
    }

    public void enableLog(boolean enable) {
        mLogFlag = enable;
        if (mIsStarting){
            stop();
            start();
        }
    }

    /**
     * The Log Level:i
     *
     * @param str
     */
    public static void i(Object str) {
        Logger.log(INFO, TAG, str.toString(), null);
    }

    /**
     * The Log Level:d
     *
     * @param str
     */
    public static void d(Object str) {
        Logger.log(DEBUG, TAG, str.toString(), null);
    }

    /**
     * The Log Level:V
     *
     * @param str
     */
    public static void v(Object str) {
        Logger.log(VERBOSE, TAG, str.toString(), null);
    }

    /**
     * The Log Level:w
     *
     * @param str
     */
    public static void w(Object str) {
        Logger.log(WARN, TAG, str.toString(), null);
    }

    /**
     * The Log Level:e
     *
     * @param str
     */
    public static void e(Object str) {
        Logger.log(ERROR, TAG, str.toString(), null);
    }

    /**
     * The Log Level:e
     *
     * @param ex
     */
    public static void e(Exception ex) {
        Logger.log(ERROR, TAG, ex.toString(), null);
    }

    /**
     * The Log Level:e
     *
     * @param log
     * @param tr
     */
    public static void e(String log, Throwable tr) {
        Logger.log(ERROR, TAG, log, tr);
    }

}
