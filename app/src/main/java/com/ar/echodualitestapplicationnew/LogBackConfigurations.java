package com.ar.echodualitestapplicationnew;

import androidx.annotation.Keep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.StatusPrinter;

@Keep
public class LogBackConfigurations {

    public LogBackConfigurations() {
        configureLogbackDirectly();
    }

    private void configureLogbackDirectly() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        if (!new File(Constants.logFolder).isDirectory())
            new File(Constants.logFolder).mkdirs();
        final String LOG_DIR = Constants.logFolder;
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setContext(context);
        rollingFileAppender.setLazy(true);
        rollingFileAppender.setFile(LOG_DIR + File.separator + "log.txt");
        SizeBasedTriggeringPolicy<ILoggingEvent> trigPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
        trigPolicy.setMaxFileSize(Constants.MAX_LOG_FILE_SIZE);
        trigPolicy.setContext(context);
        trigPolicy.start();
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern(LOG_DIR + File.separator + "log.%i.txt");
        rollingPolicy.setMinIndex(Constants.MIN_LOG_FILE_COUNT);
        rollingPolicy.setMaxIndex(Constants.MAX_LOG_FILE_COUNT);
        rollingPolicy.start();
        rollingFileAppender.setTriggeringPolicy(trigPolicy);
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%d{dd/MM/yyyy HH:mm:ss.SSS} %logger{35} - %msg%n");
        encoder.setContext(context);
        encoder.start();
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.start();

        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(context);
        encoder2.setPattern("[%thread] %msg%n");
        encoder2.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(context);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.start();

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
        root.addAppender(rollingFileAppender);
        root.addAppender(logcatAppender);
        StatusPrinter.print(context);
    }
}


