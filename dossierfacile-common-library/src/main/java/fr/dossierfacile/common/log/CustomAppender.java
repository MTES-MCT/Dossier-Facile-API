package fr.dossierfacile.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CustomAppender extends AppenderBase<ILoggingEvent> {

    private final String uniqueIdentifier;
    private final ConcurrentMap<String, List<LogModel>> logsByIdentifier = new ConcurrentHashMap<>();

    public CustomAppender(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        String identifier = MDC.get(uniqueIdentifier);
        if (identifier != null) {
            String message = eventObject.getFormattedMessage();
            String loggedStackTrace = null;
            if (eventObject.getLevel().isGreaterOrEqual(Level.ERROR)) {
                IThrowableProxy throwableProxy = eventObject.getThrowableProxy();
                if (throwableProxy != null) {
                    StringBuilder stackTrace = new StringBuilder();
                    for (StackTraceElementProxy line : throwableProxy.getStackTraceElementProxyArray()) {
                        stackTrace.append(line).append("\n");
                    }
                    message = throwableProxy.getMessage();
                    loggedStackTrace = stackTrace.toString();
                }
            }

            LogModel log = new LogModel(eventObject.getLevel(), message, loggedStackTrace);
            logsByIdentifier.computeIfAbsent(identifier, id -> new ArrayList<>())
                    .add(log);
        }
    }

    public List<LogModel> getLogsForRequestId(String uniqueIdentifier) {
        return logsByIdentifier.getOrDefault(uniqueIdentifier, List.of());
    }

    public void clearLogsForRequest(String uniqueIdentifier) {
        logsByIdentifier.remove(uniqueIdentifier);
    }

    public static CustomAppender initCustomAppender(String uniqueIdentifier) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        var customLogger = new CustomAppender(uniqueIdentifier);
        customLogger.setContext(lc);
        customLogger.start();
        return customLogger;
    }
}
