package ca.itinerum.android.utilities;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.itinerum.android.BuildConfig;

@SuppressWarnings("HardCodedStringLiteral")
public class Logger {

	private static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public final static int VERBOSE = 2;
    public final static int DEBUG   = 3;
    public final static int INFO    = 4;
    public final static int WARN    = 5;
    public final static int ERROR   = 6;
    public final static int ASSERT  = 7;

    public static Logger l = new Logger();

    int logLevel;
    boolean autoTag;

    private Logger() {
        logLevel = WARN;

        autoTag = true;
    }

    public boolean autoTag() {
        return autoTag;
    }

    public void autoTag(boolean enable) {
        this.autoTag = enable;
    }

    public int logLevel() {
        return logLevel;
    }

    public void logLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    String generateTag() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        String tag = "datamb";

        if (autoTag) {
            int index = -1;
            for (int i = 0; i < stackTraceElements.length; i++) {
                StackTraceElement e = stackTraceElements[i];

                if (e.getMethodName().equals("getStackTrace")) {
                    index = i;
                }

                if (index != -1 && i == index+3) {
                    String className = e.getClassName().substring(e.getClassName().lastIndexOf(".")+1, e.getClassName().length());
                    tag += " "+className+"."+e.getMethodName()+":"+e.getLineNumber();
                    break;
                }
            }
        }

        return tag;
    }

    public int v(Object... args) {
        if (args == null) return -1;
        if (logLevel > VERBOSE) return -1;
        StringBuilder msg = new StringBuilder();
        for (Object a : args) {
            if (a == null) continue;
            msg.append(a);
            msg.append(" ");
        }
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg.toString());
	    return Log.v(generateTag(), msg.toString());
    }

    public int v(String msg) {
        if (logLevel <= VERBOSE) return Log.v(generateTag(), msg);
        else return -1;
    }

    public int v(String msg, Throwable tr) {
        if (logLevel <= VERBOSE) return Log.v(generateTag(), msg, tr);
        else return -1;
    }

    public int d(Object... args) {
        if (args == null) return -1;
        if (logLevel > DEBUG) return -1;
        StringBuilder msg = new StringBuilder();
        for (Object a : args) {
            if (a == null) continue;
            msg.append(a);
            msg.append(" ");
        }
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg.toString());
        return Log.d(generateTag(), msg.toString());
    }

    public int d(String msg) {
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg);
	    if (logLevel <= DEBUG) return Log.d(generateTag(), msg);
        else return -1;
    }

    public int d(String msg, Throwable tr) {
        if (logLevel <= DEBUG) return Log.d(generateTag(), msg, tr);
        else return -1;
    }

    public int i(Object... args) {
        if (args == null) return -1;
        if (logLevel > INFO) return -1;
        StringBuilder msg = new StringBuilder();
        for (Object a : args) {
            if (a == null) continue;
            msg.append(a);
            msg.append(" ");
        }
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg.toString());
	    return Log.i(generateTag(), msg.toString());
    }

    public int i(String msg) {
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg);
	    if (logLevel <= INFO) return Log.i(generateTag(), msg);
        else return -1;
    }

    public int i(String msg, Throwable tr) {
        if (logLevel <= INFO) return Log.i(generateTag(), msg, tr);
        else return -1;
    }

    public int w(Object... args) {
        if (args == null) return -1;
        if (logLevel > WARN) return -1;
        StringBuilder msg = new StringBuilder();
        for (Object a : args) {
            if (a == null) continue;
            msg.append(a);
            msg.append(" ");
        }
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg.toString());
	    return Log.w(generateTag(), msg.toString());
    }

    public int w(String msg) {
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg);
	    if (logLevel <= WARN) return Log.w(generateTag(), msg);
        else return -1;
    }

    public int w(Throwable tr) {
        if (logLevel <= WARN) return Log.w(generateTag(), tr);
        else return -1;
    }

    public int w(String msg, Throwable tr) {
        if (logLevel <= WARN) return Log.w(generateTag(), msg, tr);
        else return -1;
    }

    public int e(Object... args) {
        if (args == null) return -1;
        if (logLevel > ERROR) return -1;
        StringBuilder msg = new StringBuilder();
        for (Object a : args) {
            if (a == null) continue;
            msg.append(a);
            msg.append(" ");
        }
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg.toString());
	    return Log.e(generateTag(), msg.toString());
    }

    public int e(String msg) {
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg);
	    if (logLevel <= ERROR) return Log.e(generateTag(), msg);
        else return -1;
    }

    public int e(String msg, Throwable tr) {
        if (logLevel <= ERROR) return Log.e(generateTag(), msg, tr);
        else return -1;
    }

    public int x(Object... args) {
        if (args == null) return -1;
        if (!BuildConfig.DEBUG) return -1;
        StringBuilder msg = new StringBuilder();
        for (Object a : args) {
            if (a == null) continue;
            msg.append(a);
            msg.append(" ");
        }
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg.toString());
	    return Log.w("xx-datamb-xx", msg.toString());
    }

    public int x(String msg) {
	    if (BuildConfig.LOGFILE) appendLog(generateTag() + " *** " + msg);
	    if (!BuildConfig.DEBUG) return -1;
        return Log.w("xx-datamb-xx", msg);
    }

    public int x(Throwable tr) {
        if (!BuildConfig.DEBUG) return -1;
        return Log.w("xx-datamb-xx", tr);
    }

    public int x(String msg, Throwable tr) {
        if (!BuildConfig.DEBUG) return -1;
        return Log.w("xx-datamb-xx", msg, tr);
    }

    public void appendLog(String text) {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
			//BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			Date date = new Date();
			String datetime = dateformat.format(date);
			buf.append(datetime);
			buf.append(" - ");
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
