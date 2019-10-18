package com.richard.weger.wqc.util;

import com.google.android.gms.common.util.Strings;
import com.richard.weger.wqc.result.ErrorResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerManager {
    static private FileHandler infoHandler, warnHandler, severeHandler;

    public static Logger getLogger(Class<?> clz){
        Logger logger = Logger.getLogger(clz.getName());
        logger.setLevel(Level.ALL);
        infoHandler = getFileHandler(ErrorResult.ErrorLevel.LOG, infoHandler);
        warnHandler = getFileHandler(ErrorResult.ErrorLevel.WARNING, warnHandler);
        severeHandler = getFileHandler(ErrorResult.ErrorLevel.SEVERE, severeHandler);
        logger.addHandler(infoHandler);
        logger.addHandler(warnHandler);
        logger.addHandler(severeHandler);
        return logger;
    }

    public static void log(Class<?> clz, ErrorResult err){
        Logger logger = getLogger(clz);
        String message = ErrorUtil.getErrorMessageWithCode(err);
        if(Strings.isEmptyOrWhitespace(message)){
           message = ErrorUtil.getUnknownErrorMessage();
        }
        if(err.getLevel() == ErrorResult.ErrorLevel.SEVERE){
            logger.severe(message);
        } else {
            logger.warning(message);
        }
    }

    public static void log(Class<?> clz, String message, ErrorResult.ErrorLevel level){
        Logger logger = getLogger(clz);
        if(level == ErrorResult.ErrorLevel.LOG)
            logger.info(message);
        else if(level == ErrorResult.ErrorLevel.SEVERE)
            logger.severe(message);
        else
            logger.warning(message);
    }

    private static FileHandler getFileHandler(ErrorResult.ErrorLevel level, FileHandler handler){
        if(handler == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", App.getContext().getResources().getConfiguration().getLocales().get(0));
            File pFolder = App.getContext().getExternalFilesDir(null);
            String pattern = null;
            if (pFolder != null) {
                pattern = pFolder.getPath() + "/";
                pattern += level.toString() + "_Logging_" + sdf.format(Calendar.getInstance().getTimeInMillis()) + ".txt";
            }
            try {
                handler = new FileHandler(pattern, true) {
                    @Override
                    public synchronized void publish(final LogRecord record) {
                        super.publish(record);
                        flush();
                    }
                };
                handler.setFormatter(new SimpleFormatter());
                switch (level) {
                    case SEVERE:
                        handler.setLevel(Level.SEVERE);
                        break;
                    case WARNING:
                        handler.setLevel(Level.WARNING);
                        break;
                    case LOG:
                        handler.setLevel(Level.INFO);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return handler;
    }

}
