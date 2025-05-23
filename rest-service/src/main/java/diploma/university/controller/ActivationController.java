//package diploma.university.controller;
//
//import diploma.university.service.UserActivationService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RequestMapping("/user")
//@RestController
//public class ActivationController {
//    private final UserActivationService userActivationService;
//
//    public ActivationController(UserActivationService userActivationService) {
//        this.userActivationService = userActivationService;
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value = "/activation")
//    public ResponseEntity<?> activation(@RequestParam("id") String id){
//        var res = userActivationService.activation(id);
//        if (res){
//            return ResponseEntity.ok().body("Реєстрацію успішно завершено!");
//        }
//        return ResponseEntity.internalServerError().build();
//    }
//}
package diploma.university.controller;

import diploma.university.service.UserActivationService;
import diploma.university.service.enums.ActivationStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user")
@RestController
public class ActivationController {
    private final UserActivationService userActivationService;

    public ActivationController(UserActivationService userActivationService) {
        this.userActivationService = userActivationService;
    }

    @GetMapping("/activation")
    public ResponseEntity<String> activation(@RequestParam("id") String id){
        ActivationStatus status = userActivationService.activation(id);
        switch (status) {
            case SUCCESS:
                return ResponseEntity.ok(
                        "Реєстрацію успішно завершено!");
            case USER_NOT_FOUND:
                return ResponseEntity.badRequest().body(
                        "Посилання некоректне або користувач не знайдений.");
            case ALREADY_ACTIVATED:
                return ResponseEntity.ok(
                        "Обліковий запис вже був активований.");
            default:
                return ResponseEntity.internalServerError().body(
                        "Виникла помилка на сервері. Спробуйте пізніше.");
        }
    }
}