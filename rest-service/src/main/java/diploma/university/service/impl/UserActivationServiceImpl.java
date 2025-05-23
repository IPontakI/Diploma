//package diploma.university.service.impl;
//
//import diploma.university.dao.AppUserDAO;
//import diploma.university.service.UserActivationService;
//import diploma.university.utils.CryptoTool;
//import org.springframework.stereotype.Service;
//
//@Service
//public class UserActivationServiceImpl implements UserActivationService {
//    private final AppUserDAO appUserDAO;
//    private final CryptoTool cryptoTool;
//
//    public UserActivationServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
//        this.appUserDAO = appUserDAO;
//        this.cryptoTool = cryptoTool;
//    }
//
//    @Override
//    public boolean activation(String cryptoUserId) {
//        var userId = cryptoTool.idOf(cryptoUserId);
//        var optional = appUserDAO.findById(userId);
//        if (optional.isPresent()){
//            var user = optional.get();
//            user.setIsActive(true);
//            appUserDAO.save(user);
//            return true;
//        }
//        return false;
//    }
//}
package diploma.university.service.impl;

import diploma.university.dao.AppUserDAO;
import diploma.university.entity.AppUser;
import diploma.university.service.UserActivationService;
import diploma.university.service.enums.ActivationStatus;
import diploma.university.utils.CryptoTool;
import org.springframework.stereotype.Service;

@Service
public class UserActivationServiceImpl implements UserActivationService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public ActivationStatus activation(String cryptoUserId) {
        Long userId = cryptoTool.idOf(cryptoUserId);
        if (userId == null) {
            return ActivationStatus.USER_NOT_FOUND; // id некоректний
        }
        var optional = appUserDAO.findById(userId);
        if (optional.isPresent()) {
            AppUser user = optional.get();
            if (user.getIsActive() != null && user.getIsActive()) {
                return ActivationStatus.ALREADY_ACTIVATED;
            }
            user.setIsActive(true);
            appUserDAO.save(user);
            return ActivationStatus.SUCCESS;
        }
        return ActivationStatus.USER_NOT_FOUND;
    }
}