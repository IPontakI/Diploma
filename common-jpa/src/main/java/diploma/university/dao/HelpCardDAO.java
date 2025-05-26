package diploma.university.dao;

import diploma.university.entity.AppUser;
import diploma.university.entity.HelpCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HelpCardDAO extends JpaRepository<HelpCard, Long> {
    List<HelpCard> findByCreatorAndIsDeletedFalse(AppUser creator);
    List<HelpCard> findByStatusAndIsDeletedFalse(String status);
    Optional<HelpCard> findByIdAndIsDeletedFalse(Long id);
    List<HelpCard> findByVolunteerAndStatusAndIsDeletedFalse(AppUser user, String inProcess);
}
