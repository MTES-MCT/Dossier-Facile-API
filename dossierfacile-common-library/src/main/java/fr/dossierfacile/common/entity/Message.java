package fr.dossierfacile.common.entity;

import fr.dossierfacile.common.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message implements Serializable {

    private static final long serialVersionUID = -5941768613009997108L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "creation_date")
    private LocalDateTime creationDateTime = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user")
    @ToString.Exclude
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user")
    @ToString.Exclude
    private User toUser;

    @Column(columnDefinition = "text")
    private String messageBody;

    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus;

    @Builder.Default
    @Column(columnDefinition = "boolean default false")
    private boolean customMessage = false;

    @Builder.Default
    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<DocumentDeniedReasons> documentDeniedReasons = new ArrayList<>();

    @Transient
    private String emailHtml;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Message message = (Message) o;
        return id != null && Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}