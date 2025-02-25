package fr.dossierfacile.api.front.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dossierfacile.common.log.CustomAppender;
import fr.dossierfacile.common.log.LogModel;
import fr.dossierfacile.common.utils.LoggerUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Component
public class LogAggregationFilter extends OncePerRequestFilter {
    private Logger rootLogger;
    private CustomAppender customAppender;

    final ObjectMapper objectMapper;

    public LogAggregationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        customAppender = initCustomAppender();
        rootLogger.addAppender(customAppender);
    }

    // We have to init the custom logger by code because SL4J and Spring boot has a different context and this cause error when we try to access the appender initialized by SL4J
    private CustomAppender initCustomAppender() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        var customLogger = new CustomAppender();
        customLogger.setContext(lc);
        customLogger.start();
        return customLogger;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = LoggerUtil.getRequestId();
        try {
            filterChain.doFilter(request, response);
        } finally {
            LoggerUtil.addRequestStatusToMdc(response.getStatus());
            List<LogModel> logs = customAppender.getLogsForRequestId(requestId);
            String logMessage = objectMapper.writeValueAsString(logs);

            String enrichedLogs = String.format(
                    "Request completed: URI:%s, Method:%s, Status:%d, Logs: %s",
                    request.getRequestURI(),
                    request.getMethod(),
                    response.getStatus(),
                    logMessage
            );

            Level logLevel = logs.stream().map(LogModel::getLevel).max(Comparator.comparingInt(Level::toInt)).orElse(Level.INFO);
            LoggerUtil.sendEnrichedLogs(rootLogger, logLevel, enrichedLogs);

            customAppender.clearLogsForRequest(requestId);
        }
    }
}
