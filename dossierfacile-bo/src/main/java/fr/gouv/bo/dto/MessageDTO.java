package fr.gouv.bo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    public String getMessage() {
        return message;
    }

    private String message;

    private String emailHtml;

    public String getEmailHtml() {
        return emailHtml;
    }

}
