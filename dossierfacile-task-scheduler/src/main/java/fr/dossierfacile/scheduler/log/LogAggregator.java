package fr.dossierfacile.scheduler.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dossierfacile.common.log.CustomAppender;
import fr.dossierfacile.common.utils.LoggerUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogAggregator {

    private Logger rootLogger;
    private CustomAppender customAppender;

    final ObjectMapper objectMapper;

    public LogAggregator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void init() {
        rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        customAppender = CustomAppender.initCustomAppender(LoggerUtil.PROCESS_ID);
        rootLogger.addAppender(customAppender);
    }

    public void sendLogs() {
        var processId = LoggerUtil.getProcessId();
        var logs = customAppender.getLogsForUniqueIdentifier(processId);
        try {
            var logMessages = objectMapper.writeValueAsString(logs);
            LoggerUtil.addLogs(logMessages);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        TaskStatus taskStatus = logs.stream().anyMatch(item -> item.getLevel() == Level.ERROR) ? TaskStatus.ERROR : TaskStatus.SUCCESS;
        LoggerUtil.addTaskStatus(taskStatus.name());

        var currentTime = System.currentTimeMillis();
        var executionTime = currentTime - Long.parseLong(LoggerUtil.getExecutionTime());

        LoggerUtil.addExecutionTime(executionTime);

        String enrichedLogs = String.format(
                "Task %s completed with status %s in %s ms",
                LoggerUtil.getTaskName(),
                taskStatus.name(),
                executionTime
        );

        Level logLevel = LoggerUtil.getLogLevel(logs);
        LoggerUtil.sendEnrichedLogs(rootLogger, logLevel, enrichedLogs);

        LoggerUtil.clearMDC();
    }
}
