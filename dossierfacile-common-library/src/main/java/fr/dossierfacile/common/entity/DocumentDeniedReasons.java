package fr.dossierfacile.common.entity;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@TypeDefs({
        @TypeDef(
                name = "list-type",
                typeClass = ListArrayType.class
        )
})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DocumentDeniedReasons implements Serializable {

    private static final long serialVersionUID = -2813321453107893609L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Type(type = "list-type")
    @Column(
            name = "checked_options",
            columnDefinition = "character varying[]"
    )
    private List<String> checkedOptions = new ArrayList<>();

    @Type(type = "list-type")
    @Column(
            name = "checked_options_id",
            columnDefinition = "character integer[]"
    )
    private List<Integer> checkedOptionsId = new ArrayList<>();

    @Builder.Default
    private boolean messageData = true;

    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    @ToString.Exclude
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    @ToString.Exclude
    private Document document;

    @Builder.Default
    @Column(name = "creation_date")
    private LocalDateTime creationDate = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DocumentDeniedReasons that = (DocumentDeniedReasons) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
