package fr.dossierfacile.common.log;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogModel {
    @JsonSerialize(using = LogLevelSerializer.class)
    Level level;
    String message;
    String stackTrace;

    public LogModel(Level level, String message, String stackTrace) {
        this.level = level;
        this.message = message;
        this.stackTrace = stackTrace;
    }
}
