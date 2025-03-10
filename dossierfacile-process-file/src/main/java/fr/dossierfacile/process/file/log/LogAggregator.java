package fr.dossierfacile.process.file.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dossierfacile.common.log.CustomAppender;
import fr.dossierfacile.common.log.LogModel;
import fr.dossierfacile.common.model.JobContext;
import fr.dossierfacile.common.model.JobStatus;
import fr.dossierfacile.common.utils.LoggerUtil;
import fr.dossierfacile.process.file.amqp.ActionType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
@Slf4j
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

    public void sendWorkerLogs(JobContext jobContext, ActionType actionType) {
        LoggerUtil.prepareMDCForWorker(jobContext, actionType.name());
        var logs = customAppender.getLogsForUniqueIdentifier(jobContext.getProcessId());

        try {
            var logMessages = objectMapper.writeValueAsString(logs);
            LoggerUtil.addLogs(logMessages);
        } catch (JsonProcessingException e) {
            log.error("Error while sending worker logs", e);
        }

        var jobStatus = jobContext.getJobStatus();
        if (jobStatus == null) {
            if (logs.stream().anyMatch(log -> log.getLevel() == Level.ERROR)) {
                LoggerUtil.addJobStatus(JobStatus.ERROR.name());
            } else {
                LoggerUtil.addJobStatus(JobStatus.SUCCESS.name());
            }
        }

        String enrichedLogs = String.format(
                "Action %s on Queue %s completed with status %s in %s ms",
                LoggerUtil.getAction(),
                LoggerUtil.getQueueName(),
                LoggerUtil.getJobStatus(),
                LoggerUtil.getExecutionTime()
        );

        Level logLevel = logs.stream().map(LogModel::getLevel).max(Comparator.comparingInt(Level::toInt)).orElse(Level.INFO);
        LoggerUtil.sendEnrichedLogs(rootLogger, logLevel, enrichedLogs);

        customAppender.clearLogsForRequest(jobContext.getProcessId());
        LoggerUtil.clearMDC();
    }

}
