package diploma.university.service.impl;

import diploma.university.dao.AppUserDAO;
import diploma.university.dao.RawDataDAO;
import diploma.university.entity.AppDocument;
import diploma.university.entity.AppPhoto;
import diploma.university.entity.AppUser;
import diploma.university.entity.RawData;
import diploma.university.entity.enums.UserState;
import diploma.university.exeptions.UploadFileException;
import diploma.university.service.AppUserService;
import diploma.university.service.FileService;
import diploma.university.service.MainService;
import diploma.university.service.ProducerService;
import diploma.university.service.enums.ServiceCommands;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.List;

import static diploma.university.entity.enums.UserState.BASIC_STATE;
import static diploma.university.service.enums.ServiceCommands.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final FileService fileService;
    private final AppUserService appUserService;


    public MainServiceImpl(RawDataDAO rawDataDAOl, ProducerService producerService, AppUserDAO appUserDAO, FileService fileService, AppUserService appUserService) {
        this.rawDataDAO = rawDataDAOl;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.fileService = fileService;
        this.appUserService = appUserService;
    }

    @Override
    public void processTextMessage(Update update) {
//        saveRowData(update);
//        var appUser = findOrSaveAppUser(update);
//        var userState = appUser.getState();
//        var text = update.getMessage().getText();
//        var output = "";
//
//        if (CANCEL.equals(text)){
//            output = cancelProcess(appUser);
//        } else if (BASIC_STATE.equals(userState)) {
//            output = processServiceCommand(appUser, text);
//        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
//            //TODO додати обробку емейлу
//        }else {
//            log.error("Unknown user state: " + userState);
//            output = "Невідома помилка, введіть /cancel і спробуйте ще раз!";
//        }
//
//        var chatId = update.getMessage().getChatId();
//        sendAnswer(output, chatId);
//        saveRowData(update);
//        var appUser = findOrSaveAppUser(update);
//        var userState = appUser.getState();
//        var text = update.getMessage().getText();
//        var chatId = update.getMessage().getChatId();
//        var output = "";
//
//        var serviceCommands = ServiceCommands.fromValue(text);
//        if (CANCEL.equals(serviceCommands)){
//            output = cancelProcess(appUser);
//        } else if (BASIC_STATE.equals(userState)) {
//            output = processServiceCommand(appUser, text);
//            // Якщо маркер показати меню — надсилаємо його окремо:
//            if ("__SHOW_START_MENU__".equals(output)) {
//                SendMessage startMenu = buildStartMenu(chatId);
//                producerService.produceAnswear(startMenu);
//                return;
//            }
//            sendAnswer(output, chatId);
//        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
//            output = appUserService.setEmail(appUser, text);
//        }else {
//            log.error("Unknown user state: " + userState);
//            output = "Невідома помилка, введіть /cancel і спробуйте ще раз!";
//            sendAnswer(output, chatId);
//        }
//        sendAnswer(output, chatId);
//        saveRowData(update);
//        var appUser = findOrSaveAppUser(update);
//        var userState = appUser.getState();
//        var text = update.getMessage().getText();
//        var chatId = update.getMessage().getChatId();
//        var output = "";
//
//        var serviceCommands = ServiceCommands.fromValue(text);
//
//        if (CANCEL.equals(serviceCommands)) {
//            output = cancelProcess(appUser);
//            sendAnswer(output, chatId);
//            return;
//        }
//
//        if (BASIC_STATE.equals(userState)) {
//            output = processServiceCommand(appUser, text);
//            // Якщо маркер показати меню — надсилаємо його окремо:
//            if ("__SHOW_START_MENU__".equals(output)) {
//                SendMessage startMenu = buildStartMenu(chatId);
//                producerService.produceAnswear(startMenu);
//                return;
//            }
//            sendAnswer(output, chatId);
//            return;
//        }
//
//        if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
//            output = appUserService.setEmail(appUser, text);
//            sendAnswer(output, chatId);
//            return;
//        }
//
//        log.error("Unknown user state: " + userState);
//        output = "Невідома помилка, введіть /cancel і спробуйте ще раз!";
//        sendAnswer(output, chatId);
        saveRowData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();

        var serviceCommands = ServiceCommands.fromValue(text);
        if (ServiceCommands.CANCEL.equals(serviceCommands)) {
            String output = cancelProcess(appUser);
            sendAnswer(output, chatId);
            return;
        }
        if (userState == UserState.WAIT_FOR_LOGIN_PASSWORD_STATE) {
            processLoginStep(appUser, text, chatId);
            return;
        }

        if (userState == UserState.WAIT_FOR_USERNAME_STATE
                || userState == UserState.WAIT_FOR_PASSWORD_STATE
                || userState == UserState.WAIT_FOR_PASSWORD_CONFIRM_STATE
                || userState == UserState.WAIT_FOR_ROLE_STATE
                || userState == UserState.WAIT_FOR_PHONE_STATE
                || userState == UserState.WAIT_FOR_EMAIL_STATE) {
            processRegistrationStep(appUser, text, chatId);
            return;
        }

        String output = processServiceCommand(appUser, text);
        if (output == null) return;
        if ("__SHOW_START_MENU__".equals(output)) {
            SendMessage startMenu = buildStartMenu(chatId);
            producerService.produceAnswear(startMenu);
            return;
        }
        sendAnswer(output, chatId);
    }

    private void processLoginStep(AppUser appUser, String text, Long chatId) {
        if (appUser.getPassword() != null && appUser.getPassword().equals(text)) {
            appUser.setState(UserState.BASIC_STATE);
            appUserDAO.save(appUser);
            sendAnswer("Ви в системі!", chatId);
            // Метод для килику основного меню
        } else {
            sendAnswer("Неправильний пароль! Введіть ще раз:", chatId);
        }
    }

    private void processRegistrationStep(AppUser appUser, String text, Long chatId) {
        switch (appUser.getState()) {
            case WAIT_FOR_USERNAME_STATE:
                appUser.setUsername(text);
                appUser.setState(UserState.WAIT_FOR_PASSWORD_STATE);
                appUserDAO.save(appUser);
                sendAnswer("Введіть пароль:", chatId);
                break;

            case WAIT_FOR_PASSWORD_STATE:
                if (text.length() < 6) {
                    sendAnswer("Пароль має містити мінімум 6 символів. Введіть пароль ще раз:", chatId);
                    break;
                }
                appUser.setTempPassword(text);
                appUser.setState(UserState.WAIT_FOR_PASSWORD_CONFIRM_STATE);
                appUserDAO.save(appUser);
                sendRestartAnswer("Підтвердіть ваш пароль:", chatId, true);
                break;

            case WAIT_FOR_PASSWORD_CONFIRM_STATE:
                if (text.equalsIgnoreCase("Почати введення паролю спочатку")) {
                    appUser.setTempPassword(null);
                    appUser.setState(UserState.WAIT_FOR_PASSWORD_STATE);
                    appUserDAO.save(appUser);
                    sendAnswer("Введіть ваш новий пароль:", chatId);
                    break;
                }
                if (!text.equals(appUser.getTempPassword())) {
                    appUser.setState(UserState.WAIT_FOR_PASSWORD_STATE);
                    appUserDAO.save(appUser);
                    sendAnswer("Паролі не співпадають! Введіть пароль ще раз:", chatId);
                    break;
                }
                appUser.setPassword(appUser.getTempPassword());
                appUser.setTempPassword(null);
                appUser.setState(UserState.WAIT_FOR_ROLE_STATE);
                appUserDAO.save(appUser);
                sendRoleKeyboard(chatId); // <-- Покажемо одразу кнопки!
                break;

            case WAIT_FOR_ROLE_STATE:
                if (text.equalsIgnoreCase("Волонтер") || text.equalsIgnoreCase("Потерпілий")) {
                    String role = text.equalsIgnoreCase("Волонтер") ? "VOLUNTEER" : "VICTIM";
                    appUser.setRole(role);
                    appUser.setState(UserState.WAIT_FOR_PHONE_STATE);
                    appUserDAO.save(appUser);
                    sendAnswerAndRemoveKeyboard("Введіть номер телефону у форматі +380XXXXXXXXX:", chatId);
                } else {
                    sendRoleKeyboard(chatId);
                }
                break;

            case WAIT_FOR_PHONE_STATE:
                // Додаємо валідацію номера
                if (!text.matches("^\\+380\\d{9}$")) {
                    sendAnswer("Некоректний формат номеру! Приклад: +380660174948. Введіть номер ще раз:", chatId);
                    break;
                }
                appUser.setPhoneNumber(text);
                appUser.setState(UserState.WAIT_FOR_EMAIL_STATE);
                appUserDAO.save(appUser);
                sendAnswer("Введіть вашу email-адресу:", chatId);
                break;

            case WAIT_FOR_EMAIL_STATE:
                String output = appUserService.setEmail(appUser, text);
                appUserDAO.save(appUser);
                sendAnswer(output, chatId);
                break;

            default:
                sendAnswer("Сталася невідома помилка під час реєстрації. Введіть /cancel і спробуйте ще раз.", chatId);
        }
    }

    private void sendAnswerAndRemoveKeyboard(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        producerService.produceAnswear(sendMessage);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRowData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowedToSendContent(chatId, appUser)){
            return;
        }

        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            var answer = "Документ успішно завантажено!";
            sendAnswer(answer, chatId);
        }catch (UploadFileException ex){
            log.error(ex);
            String error = "Не вдалось завантажити файл, спробуйте пізніше.";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRowData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowedToSendContent(chatId, appUser)){
            return;
        }

        try{
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            var answer = "Фото успішно завантажено!";
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            log.error(ex);
            String error = "Не вдалось завантажити фото, спробуйте пізніше.";
            sendAnswer(error, chatId);
        }
    }

    private boolean isNotAllowedToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()){
            var error = "Зареєструйтесь, або актевуйте свій обліковий запис.";
            return true;
        }else if(!BASIC_STATE.equals(userState)){
            var error = "Відміність попередню команду /cancel для надсилання повідомлень.";
            return true;
        }
        return false;
    }

    private void sendRestartAnswer(String output, Long chatId, boolean showRestart) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);

        if (showRestart) {
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setResizeKeyboard(true);

            KeyboardRow row = new KeyboardRow();
            row.add("Почати введення паролю спочатку");

            keyboardMarkup.setKeyboard(List.of(row));
            sendMessage.setReplyMarkup(keyboardMarkup);
        }

        producerService.produceAnswear(sendMessage);
    }
    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswear(sendMessage);
    }

    private void sendOnlyRegistrationButton(Long chatId, String message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("Зареєструватися");

        keyboardMarkup.setKeyboard(List.of(row));

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setReplyMarkup(keyboardMarkup);

        producerService.produceAnswear(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String text) {
        var serviceCommands = ServiceCommands.fromValue(text);

        if ("Увійти".equalsIgnoreCase(text) || LOG_IN.equals(serviceCommands)) {
            // Якщо користувач не активований (не завершив реєстрацію)
            if (appUser.getIsActive() == null || !appUser.getIsActive()) {
                appUser.setState(BASIC_STATE);
                appUserDAO.save(appUser);
                sendOnlyRegistrationButton(
                        appUser.getTelegramUserId(),
                        "Спочатку треба зареєструватись."
                );
                return null;
            }
            // Якщо активований, просимо ввести пароль
            appUser.setState(UserState.WAIT_FOR_LOGIN_PASSWORD_STATE);
            appUserDAO.save(appUser);
            String username = appUser.getUsername() != null ? appUser.getUsername() : "користувач";
            sendAnswerAndRemoveKeyboard(
                    String.format("З поверненням, %s! Для входу введіть ваш пароль:", username),
                    appUser.getTelegramUserId()
            );
            return null;
        } else if ("Зареєструватися".equalsIgnoreCase(text) || REGISTRATION.equals(serviceCommands)) {
            appUserService.registerUser(appUser);
            sendAnswerAndRemoveKeyboard("Введіть ваш нікнейм:", appUser.getTelegramUserId());
            return null;
        } else if (HELP.equals(serviceCommands)) {
            return help();
        } else if (START.equals(serviceCommands)) {
            return "__SHOW_START_MENU__";
        } else {
            return "Невідома команда! Перегляньте список доступних команд /help";
        }
    }

    private void sendRoleKeyboard(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Оберіть роль: 'VOLUNTEER' або 'VICTIM'");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("Волонтер");
        row.add("Потерпілий");

        keyboardMarkup.setKeyboard(List.of(row));
        sendMessage.setReplyMarkup(keyboardMarkup);

        producerService.produceAnswear(sendMessage);
    }

//    private String processServiceCommand(AppUser appUser, String text) {
//        var serviceCommands = ServiceCommands.fromValue(text);
//        if (REGISTRATION.equals(serviceCommands)){
//            return appUserService.registerUser(appUser);
//        }else if (LOG_IN.equals(serviceCommands)) {
//            //TODO додати логіку входу
//            return "Функція входу тимчасово недоступна.";
//        }        else if (HELP.equals(serviceCommands)){
//            return help();
//        } else if (START.equals(serviceCommands)) {
//            return "__SHOW_START_MENU__";
//        }else {
//            return "Невідома команда! Перегляньте список доступних команд /help";
//        }
//    }

    private String help() {
        return "Список доступних команд:\n"
                + "/cancel - відміна виконання нинішньої команди;\n"
                + "/registration - реєстрація користувача;\n"
                + "/log_in - вхід до існуючого облікового запису;";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда відмінена!";
    }

    private AppUser findOrSaveAppUser(Update update){
        User telegramUser = update.getMessage().getFrom();
        var optional = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if (optional.isEmpty()){
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .telegramUsername(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(false)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return optional.get();
    }

    private SendMessage buildStartMenu(Long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardRow row = new KeyboardRow();
        row.add("Увійти");
        row.add("Зареєструватися");

        keyboardMarkup.setKeyboard(List.of(row));

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(
                "Вітаємо у Волонтерському чат-боті!\n\n" +
                        "Цей бот допоможе організувати підтримку між волонтерами та потребуючими у допомозі. " +
                        "Ви можете створити заявку на допомогу або стати волонтером, щоб допомогти іншим.\n\n" +
                        "Оберіть потрібну дію:"
        );
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    private void saveRowData(Update update) {
        RawData rawData = RawData.builder().event(update).build();
        rawDataDAO.save(rawData);
    }
}
