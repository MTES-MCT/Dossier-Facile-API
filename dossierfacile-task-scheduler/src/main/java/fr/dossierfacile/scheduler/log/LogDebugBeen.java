package fr.dossierfacile.scheduler.log;

import fr.dossierfacile.scheduler.tasks.ademe.CheckAdemeApiTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class LogDebugBeen {

    private final CheckAdemeApiTask checkAdemeApiTask;

    public LogDebugBeen(CheckAdemeApiTask checkAdemeApiTask) {
        this.checkAdemeApiTask = checkAdemeApiTask;
    }

    @PostConstruct
    public void init() {
        log.info("LogDebugBeen init");
        checkAdemeApiTask.checkAdemeApi();
    }

}
