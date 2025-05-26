package diploma.university.service;

import diploma.university.entity.enums.CardImportance;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardCreationSession {
    private String title;
    private String description;
    private String contact;
    private CardImportance importance;
}
