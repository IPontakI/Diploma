package diploma.university.entity;

import diploma.university.entity.enums.CardImportance;
import lombok.*;

import javax.persistence.*;

@Setter
@Getter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "help_card")
public class HelpCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String contact;
    @Enumerated(EnumType.STRING)
    private CardImportance importance;
    @ManyToOne
    private AppUser creator;
    @ManyToOne
    private AppUser volunteer;
    private String status;
    private Boolean isDeleted = false;
}
