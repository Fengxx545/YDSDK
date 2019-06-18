package com.yunding.ydbaselib.ydlog;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogStrategy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.yunding.ydbaselib.ydlog.YDLogUtils.checkNotNull;


public class YDDiskLogFormatStrategy implements FormatStrategy{
    private static final String YD_NEW_LINE = "\n";
    private static final String YD_SEPARATOR = " ";
    private static final String YD_BRACKET_LEFT= "[";
    private static final String YD_BRACKET_RIGHT= "]";
    private static final String YD_ROW_ROD= " - ";
    private static final String YD_FILE_NAME= "文件名：";
    private static final String YD_METHOD_NAME= "方法名：";
    private static final String YD_LINE_NUMBER= "行数：";
    private static final String YD_LINE_NUMBER_U= "行";
    private static final String YD_THREAD= "线程：";

    @NonNull private final Date mDate;
    @NonNull private final SimpleDateFormat mDateFormat;
    @NonNull private final LogStrategy mLogStrategy;
    @Nullable private final String mTag;

    private YDDiskLogFormatStrategy(@NonNull Builder builder) {
        checkNotNull(builder);

        mDate = builder.date;
        mDateFormat = builder.dateFormat;
        mLogStrategy = builder.logStrategy;
        mTag = builder.tag;
    }

    @NonNull public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String message) {
        checkNotNull(message);

        mDate.setTime(System.currentTimeMillis());

        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int stackIndex= YDLogUtils.getStackOffset(trace);
        String threadName = Thread.currentThread().getName();

        StringBuilder builder = new StringBuilder();

        // human-readable mDate/time
        builder.append(mDateFormat.format(mDate));
        builder.append(YD_SEPARATOR);

        // level

        builder.append(YD_BRACKET_LEFT);
        builder.append(YDLogUtils.logLevel(priority));
        builder.append(YD_BRACKET_RIGHT);
        builder.append(YD_SEPARATOR);

        builder.append(YD_BRACKET_LEFT);

        // thread
        if (threadName != null) {
            builder.append(YD_THREAD);
            builder.append(threadName);
            builder.append(YD_SEPARATOR);
        }

        // class and method
        if (stackIndex <= trace.length - 1) {
            builder.append(YD_FILE_NAME);
            builder.append(trace[stackIndex].getFileName());
            builder.append(YD_SEPARATOR);
            builder.append(YD_LINE_NUMBER);
            builder.append(trace[stackIndex].getLineNumber());
            builder.append(YD_LINE_NUMBER_U);
            builder.append(YD_SEPARATOR);
            builder.append(YD_METHOD_NAME);
            builder.append(trace[stackIndex].getMethodName());
        } else  {
            // TODO
        }

        builder.append(YD_BRACKET_RIGHT);

        // message
        builder.append(YD_ROW_ROD);
        builder.append(message);

        builder.append(YD_NEW_LINE);

        mLogStrategy.log(priority, tag, builder.toString());
    }

    public static final class Builder {
        Date date;
        SimpleDateFormat dateFormat;
        LogStrategy logStrategy;
        String tag = "YD_PRETTY_LOGGER";

        private Builder() {
        }

        @NonNull public YDDiskLogFormatStrategy.Builder date(@Nullable Date val) {
            date = val;
            return this;
        }

        @NonNull public YDDiskLogFormatStrategy.Builder dateFormat(@Nullable SimpleDateFormat val) {
            dateFormat = val;
            return this;
        }

        @NonNull public YDDiskLogFormatStrategy.Builder logStrategy(@Nullable LogStrategy val) {
            logStrategy = val;
            return this;
        }

        @NonNull public YDDiskLogFormatStrategy.Builder tag(@Nullable String tag) {
            this.tag = tag;
            return this;
        }

        @NonNull public YDDiskLogFormatStrategy build() {
            if (date == null) {
                date = new Date();
            }

            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.CHINA);
            }
            return new YDDiskLogFormatStrategy(this);
        }
    }
}
