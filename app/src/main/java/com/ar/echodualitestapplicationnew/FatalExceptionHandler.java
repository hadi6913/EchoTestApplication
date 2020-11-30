package com.ar.echodualitestapplicationnew;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FatalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static Logger log = LoggerFactory.getLogger(FatalExceptionHandler.class);
    private Activity activity;

    public FatalExceptionHandler(Activity a){
        activity = a;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        log.error("FatalExceptionHandler : " + "Exception occurred, go to restart / " + throwable.getMessage()+"/"+throwable.getLocalizedMessage());
        StringWriter errors = new StringWriter();
        throwable.printStackTrace(new PrintWriter(errors));
        log.error("FatalExceptionHandler", errors.toString());
        activity.finish();
        System.exit(2);
    }
}


