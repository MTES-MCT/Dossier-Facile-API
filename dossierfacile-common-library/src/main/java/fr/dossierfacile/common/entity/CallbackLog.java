package fr.dossierfacile.common.entity;


import fr.dossierfacile.common.enums.TenantFileStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="callback_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CallbackLog {

    private static final long serialVersionUID = -3603846262626208324L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long tenant_id;

    @Column
    private Long partner_id;

    @Column
    @Enumerated(EnumType.STRING)
    private TenantFileStatus tenant_status;

    @Column(name = "creation_date")
    private LocalDateTime creationDateTime = LocalDateTime.now();

    @Column
    private String content_json;

    public CallbackLog(Long tenant_id, Long partner_id, TenantFileStatus tenant_status, String content_json){
        this.tenant_id = tenant_id;
        this.partner_id = partner_id;
        this.tenant_status = tenant_status;
        this.content_json = content_json;
    }
}
