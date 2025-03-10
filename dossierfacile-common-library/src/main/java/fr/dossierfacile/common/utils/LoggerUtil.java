package fr.dossierfacile.common.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import fr.dossierfacile.common.log.LogModel;
import fr.dossierfacile.common.model.JobContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoggerUtil {
    public static final String REQUEST_ID = "request_id";
    public static final String PROCESS_ID = "process_id";

    private static final String NUMBER_REGEX = "(?<=/)(\\d+)(?=[/?]|$)";
    private static final String UUID_REGEX = "(?<=/)([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})(?=[/?]|$)";
    private static final String EMAIL_REGEX = "(?<=/)([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})(?=[/?]|$)";

    private static final String API_URL = "uri";
    private static final String API_NORMALIZED_URI = "normalized_uri";
    private static final String API_RESPONSE_STATUS = "response_status";
    private static final String API_REAL_IP = "ip";
    private static final String API_BODY = "body";
    private static final String API_FORM_PARAMETERS = "form_parameters";
    private static final String API_PARAMS = "params";
    private static final String API_RESPONSE_CONTENT_TYPE = "response_content_type";
    private static final String API_RESPONSE_BODY = "response_body";

    private static final String PROCESS_QUEUE_NAME = "queue_name";
    private static final String PROCESS_FILE_ID = "file_id";
    private static final String PROCESS_DOCUMENT_ID = "document_id";
    private static final String PROCESS_ACTION = "action";
    private static final String PROCESS_EXECUTION_START = "execution_start";
    private static final String PROCESS_JOB_STATUS = "job_status";

    private static final String TASK_NAME = "task_name";
    private static final String TASK_STATUS = "task_status";

    private static final String LOGS = "logs";
    private static final String EXECUTION_TIME = "execution_time";


    public static void sendEnrichedLogs(Level level, String message) {
        var rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        sendEnrichedLogs(rootLogger, level, message);
    }

    public static Level getLogLevel(List<LogModel> logs) {
        return logs.stream().map(LogModel::getLevel).max(Comparator.comparingInt(Level::toInt)).orElse(Level.INFO);
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
        MDC.put(API_URL, request.getRequestURI());
        MDC.put(API_NORMALIZED_URI, LoggerUtil.normalizeUrl(request.getRequestURI()));
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());
        MDC.put(API_REAL_IP, request.getHeader("X-Real-Ip")); // specific to Scalingo infra
        additionalContextElements.forEach(MDC::put);
    }

    public static void prepareMDCForWorker(JobContext jobContext, String actionType) {
        var elapsedTime = System.currentTimeMillis() - jobContext.getStartTime();
        MDC.put(PROCESS_FILE_ID, String.valueOf(jobContext.getFileId()));
        MDC.put(PROCESS_ACTION, actionType);
        MDC.put(PROCESS_EXECUTION_START, String.valueOf(jobContext.getStartTime()));
        MDC.put(EXECUTION_TIME, String.valueOf(elapsedTime));
        MDC.put(PROCESS_DOCUMENT_ID, String.valueOf(jobContext.getDocumentId()));
        MDC.put(PROCESS_QUEUE_NAME, jobContext.getQueueName().name());
    }

    public static void prepareMDCForScheduledTask(String taskName) {
        var startTime = String.valueOf(System.currentTimeMillis());
        MDC.put(PROCESS_ID, UUID.randomUUID().toString());
        MDC.put(EXECUTION_TIME, startTime);
        MDC.put(TASK_NAME, taskName);
    }

    public static void addRequestStatusToMdc(int status) {
        MDC.put(API_RESPONSE_STATUS, String.valueOf(status));
    }

    public static void addRequestBody(String body) {
        MDC.put(API_BODY, String.valueOf(body));
    }

    public static void addRequestParams(String params) {
        MDC.put(API_PARAMS, String.valueOf(params));
    }

    public static void addRequestFormParameters(String parameters) {
        MDC.put(API_FORM_PARAMETERS, String.valueOf(parameters));
    }

    public static void addResponseContentType(String contentType) {
        MDC.put(API_RESPONSE_CONTENT_TYPE, String.valueOf(contentType));
    }

    public static void addResponseBody(String body) {
        MDC.put(API_RESPONSE_BODY, String.valueOf(body));
    }

    public static void addExecutionTime(long responseTime) {
        MDC.put(EXECUTION_TIME, String.valueOf(responseTime));
    }
    public static String getExecutionTime() {
        return MDC.get(EXECUTION_TIME);
    }

    public static void addProcessId(String processId) {
        MDC.put(PROCESS_ID, String.valueOf(processId));
    }

    public static String getProcessQueueName() {
        return MDC.get(PROCESS_QUEUE_NAME);
    }

    public static void addLogs(String logs) {
        MDC.put(LOGS, String.valueOf(logs));
    }

    public static String getRequestId() {
        return MDC.get(REQUEST_ID);
    }

    public static String getProcessId() {
        return MDC.get(PROCESS_ID);
    }

    public static void addJobStatus(String jobStatus) {
        MDC.put(PROCESS_JOB_STATUS, jobStatus);
    }

    public static String getProcessJobStatus() {
        return MDC.get(PROCESS_JOB_STATUS);
    }

    public static String getProcessAction() {
        return MDC.get(PROCESS_ACTION);
    }

    public static void addTaskStatus(String taskStatus) {
        MDC.put(TASK_STATUS, taskStatus);
    }

    public static String getTaskName() {
        return MDC.get(TASK_NAME);
    }

    public static void clearMDC() {
        MDC.clear();
    }
}
