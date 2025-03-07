package fr.dossierfacile.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CustomAppender extends AppenderBase<ILoggingEvent> {

    private final ConcurrentMap<String, List<LogModel>> logsByRequestId = new ConcurrentHashMap<>();

    @Override
    protected void append(ILoggingEvent eventObject) {
        String requestId = MDC.get("request_id");
        if (requestId != null) {
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
            logsByRequestId.computeIfAbsent(requestId, id -> new ArrayList<>())
                    .add(log);
        }
    }

    public List<LogModel> getLogsForRequestId(String requestId) {
        return logsByRequestId.getOrDefault(requestId, List.of());
    }

    public void clearLogsForRequest(String requestId) {
        logsByRequestId.remove(requestId);
    }
}
