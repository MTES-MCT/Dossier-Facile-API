package fr.dossierfacile.common.model;

import fr.dossierfacile.common.entity.messaging.QueueName;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class JobContext {

    private final String processId;
    @Setter
    private JobStatus jobStatus;
    private final Long startTime;
    private final Long documentId;
    private final Long fileId;
    private final QueueName queueName;

    public JobContext(Long documentId, Long fileId, QueueName queueName) {
        this.processId = UUID.randomUUID().toString();
        this.startTime = System.currentTimeMillis();
        this.documentId = documentId;
        this.fileId = fileId;
        this.queueName = queueName;
    }

}
