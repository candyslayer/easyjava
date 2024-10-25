package com.easyjava;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;

public class LogbackConfig  {
        @SuppressWarnings("unchecked")
        public static void configureLogback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setCharset(java.nio.charset.Charset.forName("utf-8"));
        encoder.setPattern("%date{yyyy-MM-dd HH:mm:ss} [%thread] ==> %level %logger{10} - %msg%n");
        encoder.start();
        
        @SuppressWarnings("rawtypes")
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        //之前我这里设置的是root导致设置的setAdditive不起作用，应该设置所需要拦截向上传递父类的包，避免打印两次日志
        ch.qos.logback.classic.Logger rootLogger = context.getLogger("com.easyjava.builder");
        rootLogger.addAppender(consoleAppender);
        rootLogger.setAdditive(false);
    }
}
