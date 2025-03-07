package fr.dossierfacile.common.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

public class LoggerUtil {

    private static final String NUMBER_REGEX = "(?<=/)(\\d+)(?=[/?]|$)";
    private static final String UUID_REGEX = "(?<=/)([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})(?=[/?]|$)";
    private static final String EMAIL_REGEX = "(?<=/)([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})(?=[/?]|$)";

    private static final String URI = "uri";
    private static final String NORMALIZED_URI = "normalized_uri";
    private static final String REQUEST_ID = "request_id";
    private static final String RESPONSE_STATUS = "response_status";
    private static final String REAL_IP = "ip";
    private static final String BODY = "body";
    private static final String FORM_PARAMETERS = "form_parameters";
    private static final String PARAMS = "params";
    private static final String RESPONSE_TIME = "elapsed_time";
    private static final String LOGS = "logs";
    private static final String RESPONSE_CONTENT_TYPE = "response_content_type";
    private static final String RESPONSE_BODY = "response_body";


    public static void sendEnrichedLogs(Level level, String message) {
        var rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        sendEnrichedLogs(rootLogger, level, message);
    }

    public static void sendEnrichedLogs(Logger logger, Level level, String message) {
        LoggingEvent enrichedEvent = new LoggingEvent();
        enrichedEvent.setTimeStamp(System.currentTimeMillis());
        enrichedEvent.setLoggerName("AggregatorLogger");
        enrichedEvent.setLevel(level);
        enrichedEvent.setThreadName(Thread.currentThread().getName());
        enrichedEvent.setMessage(message);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        enrichedEvent.setLoggerContext(loggerContext);

        logger.callAppenders(enrichedEvent);
    }

    public static String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        // Remplacement des IDs numériques
        url = url.replaceAll(NUMBER_REGEX, "{id}");
        // Remplacement des UUIDs
        url = url.replaceAll(UUID_REGEX, "{uuid}");
        url = url.replaceAll(EMAIL_REGEX, "{email}");
        return url;
    }

    public static void prepareMDC(HttpServletRequest request, Map<String, String> additionalContextElements) {
        MDC.put(URI, request.getRequestURI());
        MDC.put(NORMALIZED_URI, LoggerUtil.normalizeUrl(request.getRequestURI()));
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());
        MDC.put(REAL_IP, request.getHeader("X-Real-Ip")); // specific to Scalingo infra
        additionalContextElements.forEach(MDC::put);
    }

    public static void addRequestStatusToMdc(int status) {
        MDC.put(RESPONSE_STATUS, String.valueOf(status));
    }

    public static void addRequestBody(String body) {
        MDC.put(BODY, String.valueOf(body));
    }

    public static void addRequestParams(String params) {
        MDC.put(PARAMS, String.valueOf(params));
    }

    public static void addRequestFormParameters(String parameters) {
        MDC.put(FORM_PARAMETERS, String.valueOf(parameters));
    }

    public static void addResponseContentType(String contentType) {
        MDC.put(RESPONSE_CONTENT_TYPE, String.valueOf(contentType));
    }

    public static void addResponseBody(String body) {
        MDC.put(RESPONSE_BODY, String.valueOf(body));
    }

    public static void addResponseTime(long responseTime) {
        MDC.put(RESPONSE_TIME, String.valueOf(responseTime));
    }

    public static void addLogs(String logs) {
        MDC.put(LOGS, String.valueOf(logs));
    }

    public static String getRequestId() {
        return MDC.get(REQUEST_ID);
    }

    public static void clearMDC() {
        MDC.clear();
    }
}
