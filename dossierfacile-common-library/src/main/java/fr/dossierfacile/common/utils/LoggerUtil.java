package fr.dossierfacile.common.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import fr.dossierfacile.common.model.JobContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

public class LoggerUtil {
    public static final String REQUEST_ID = "request_id";
    public static final String PROCESS_ID = "process_id";

    private static final String NUMBER_REGEX = "(?<=/)(\\d+)(?=[/?]|$)";
    private static final String UUID_REGEX = "(?<=/)([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})(?=[/?]|$)";
    private static final String EMAIL_REGEX = "(?<=/)([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})(?=[/?]|$)";

    private static final String URI = "uri";
    private static final String NORMALIZED_URI = "normalized_uri";
    private static final String RESPONSE_STATUS = "response_status";
    private static final String REAL_IP = "ip";
    private static final String BODY = "body";
    private static final String FORM_PARAMETERS = "form_parameters";
    private static final String PARAMS = "params";
    private static final String EXECUTION_TIME = "execution_time";
    private static final String LOGS = "logs";
    private static final String RESPONSE_CONTENT_TYPE = "response_content_type";
    private static final String RESPONSE_BODY = "response_body";

    private static final String QUEUE_NAME = "queue_name";
    private static final String FILE_ID = "file_id";
    private static final String DOCUMENT_ID = "document_id";
    private static final String ACTION = "action";
    private static final String EXECUTION_START = "execution_start";
    private static final String JOB_STATUS = "job_status";


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
        enrichedEvent.setMDCPropertyMap(MDC.getCopyOfContextMap());

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        enrichedEvent.setLoggerContext(loggerContext);

        logger.callAppenders(enrichedEvent);
    }

    public static String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        // Replacement of numeric ids
        url = url.replaceAll(NUMBER_REGEX, "{id}");
        // Replacement of UUIDs
        url = url.replaceAll(UUID_REGEX, "{uuid}");
        // Replacement of Emails
        url = url.replaceAll(EMAIL_REGEX, "{email}");
        return url;
    }

    public static void prepareMDCForHttpRequest(HttpServletRequest request, Map<String, String> additionalContextElements) {
        MDC.put(URI, request.getRequestURI());
        MDC.put(NORMALIZED_URI, LoggerUtil.normalizeUrl(request.getRequestURI()));
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());
        MDC.put(REAL_IP, request.getHeader("X-Real-Ip")); // specific to Scalingo infra
        additionalContextElements.forEach(MDC::put);
    }

    public static void prepareMDCForWorker(JobContext jobContext, String actionType) {
        var elapsedTime = System.currentTimeMillis() - jobContext.getStartTime();
        MDC.put(FILE_ID, String.valueOf(jobContext.getFileId()));
        MDC.put(ACTION, actionType);
        MDC.put(EXECUTION_START, String.valueOf(jobContext.getStartTime()));
        MDC.put(EXECUTION_TIME, String.valueOf(elapsedTime));
        MDC.put(DOCUMENT_ID, String.valueOf(jobContext.getDocumentId()));
        MDC.put(QUEUE_NAME, jobContext.getQueueName().name());
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

    public static void addElapsedTime(long responseTime) {
        MDC.put(EXECUTION_TIME, String.valueOf(responseTime));
    }
    public static String getExecutionTime() {
        return MDC.get(EXECUTION_TIME);
    }

    public static void addProcessId(String processId) {
        MDC.put(PROCESS_ID, String.valueOf(processId));
    }

    public static String getQueueName() {
        return MDC.get(QUEUE_NAME);
    }

    public static void addLogs(String logs) {
        MDC.put(LOGS, String.valueOf(logs));
    }

    public static String getRequestId() {
        return MDC.get(REQUEST_ID);
    }

    public static void addJobStatus(String jobStatus) {
        MDC.put(JOB_STATUS, jobStatus);
    }

    public static String getJobStatus() {
        return MDC.get(JOB_STATUS);
    }

    public static String getAction() {
        return MDC.get(ACTION);
    }

    public static void clearMDC() {
        MDC.clear();
    }
}
