package fr.dossierfacile.scheduler.tasks;


import fr.dossierfacile.common.utils.LoggerUtil;
import fr.dossierfacile.scheduler.log.LogAggregator;
import org.springframework.beans.factory.annotation.Lookup;

public class AbstractTask {

    @Lookup
    protected LogAggregator logAggregator() {
        return null; // Spring will implement this @Lookup method
    }

    protected void startTask(TaskName taskName) {
        LoggerUtil.prepareMDCForScheduledTask(taskName.name());
    }

    protected void endTask() {
        logAggregator().sendLogs();
    }

}
