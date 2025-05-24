package diploma.university.service.impl;

import diploma.university.dao.AppUserDAO;
import diploma.university.dto.MailParams;
import diploma.university.entity.AppUser;
import diploma.university.service.AppUserService;
import diploma.university.utils.CryptoTool;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static diploma.university.entity.enums.UserState.*;

@Log4j
@Service
public class AppUserServiceImpl implements AppUserService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.uri}")
    private String mailServiceUri;

    public AppUserServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(AppUser appUser) {
        if (appUser.getIsActive()){
            return "Ви вже зареєстровані, увійдіь в ваш акаунт.";
        } else if (appUser.getEmail() != null) {
            return "На вашу пошту було надіслано лист для верифікації, " +
                    "перевірте розділ spam.";
        }
        appUser.setState(WAIT_FOR_EMAIL_STATE);
        appUserDAO.save(appUser);
        return "Введіть ваш email:";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try{
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException e) {
            return  "Введіть, будь ласка, коректну адресу електронної пошти.  \n" +
                    "Для скасування реєстрації натисніть /cancel.\n";
        }
        var optional = appUserDAO.findByEmail(email);
        if (optional.isEmpty()){
            appUser.setEmail(email);
            appUser.setState(BASIC_STATE);
            appUser = appUserDAO.save(appUser);

            var cryptoUserId = cryptoTool.hashOf(appUser.getId());
            var response = sendRequestToMailService(cryptoUserId, email);
            if (response.getStatusCode() != HttpStatus.OK){
                var msg = String.format(
                        "Не вдалося надіслати лист на адресу %s.  \n" +
                                "Перевірте коректність введеної електронної пошти або спробуйте ще раз пізніше.\n",email);
                log.error(msg);
                appUser.setEmail(null);
                appUserDAO.save(appUser);
                return msg;
            }
            return "Лист із посиланням для підтвердження реєстрації надіслано на вашу електронну пошту.  \n" +
                    "Перейдіть за посиланням у листі, щоб завершити реєстрацію.";
        }else {
            return  "Ця електронна адреса вже використовується.  \n" +
                    "Введіть іншу адресу або натисніть /cancel для скасування реєстрації.";
        }
    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId,
                                                            String email) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var mailParams = MailParams.builder()
                .id(cryptoUserId)
                .emailTo(email)
                .build();
        var request = new HttpEntity<>(mailParams, headers);
        return restTemplate.exchange(mailServiceUri,
                HttpMethod.POST,
                request,
                String.class);

    }
}
