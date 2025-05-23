package diploma.university.dao;

import diploma.university.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByTelegramUserId(Long aLong);
    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByEmail(String email);
}
