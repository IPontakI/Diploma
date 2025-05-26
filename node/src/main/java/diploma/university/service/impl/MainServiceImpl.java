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
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

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

    private final Map<Long, String> editActionMap = new ConcurrentHashMap<>();

    private static final String BTN_CARDS = "Картки";
    private static final String BTN_PROFILE = "Профіль";
    private static final String BTN_SELECTED_TASKS = "Обрані завдання";
    private static final String BTN_GATHERINGS = "Збори";
    private static final String BTN_CREATE_CARD = "Створити картку";
    private static final String BTN_MY_CARDS = "Мої картки";
    private static final String BTN_FAQ = "FAQ";
    private static final String BTN_VOLUNTEER = "Волонтер";
    private static final String BTN_VICTIM = "Заявник";
    private static final String BTN_CHECK_ACTIVATION = "Я активував акаунт";


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

        Long userId = appUser.getTelegramUserId();
        if (editActionMap.containsKey(userId)) {
            String action = editActionMap.get(userId);
            switch (action) {
                case "NICKNAME":
                    appUser.setUsername(text);
                    appUserDAO.save(appUser);
                    editActionMap.remove(userId);
                    sendAnswer("Нікнейм успішно оновлено!", chatId);

                    // Показуємо оновлений профіль:
                    String profileText = buildProfileInfo(appUser);
                    SendMessage msg1 = new SendMessage();
                    msg1.setChatId(chatId);
                    msg1.setText(profileText);
                    msg1.setReplyMarkup(buildProfileKeyboard());
                    producerService.produceAnswer(msg1);
                    return;
                case "PHONE":
                    if (!text.matches("^\\+380\\d{9}$")) {
                        sendAnswer("Некоректний формат номеру! Приклад: +380660174948. Введіть номер ще раз:", chatId);
                        return;
                    }
                    appUser.setPhoneNumber(text);
                    appUserDAO.save(appUser);
                    editActionMap.remove(userId);
                    sendAnswer("Телефон успішно оновлено!", chatId);

                    // Показуємо оновлений профіль:
                    String profileText2 = buildProfileInfo(appUser);
                    SendMessage msg2 = new SendMessage();
                    msg2.setChatId(chatId);
                    msg2.setText(profileText2);
                    msg2.setReplyMarkup(buildProfileKeyboard());
                    producerService.produceAnswer(msg2);
                    return;
            }
        }
        if (editActionMap.containsKey(userId) && "WAIT_FOR_PROFILE_PHOTO".equals(editActionMap.get(userId))) {
            // Тут ти можеш реалізувати реальне збереження фото або просто відповідь
            editActionMap.remove(userId);
            sendAnswer("Фото профілю успішно додано!", chatId);
            return;
        }

        if (BTN_CHECK_ACTIVATION.equalsIgnoreCase(text)) {
            appUser = appUserDAO.findByTelegramUserId(appUser.getTelegramUserId()).orElse(appUser);
            if (appUser.getIsActive() != null && appUser.getIsActive()) {
                appUser.setState(UserState.IN_MAIN_MENU);
                appUserDAO.save(appUser);
                sendMainMenu(appUser, chatId);
            } else {
                sendAnswer("Ваш акаунт ще не активовано. Перейдіть за посиланням у листі й спробуйте ще раз.", chatId);
                sendActivationCheckButton(chatId); // Ще раз показати кнопку
            }
            return;
        }
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
        if (userState == UserState.IN_MAIN_MENU) {
            processMainMenu(appUser, text, chatId);
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
            producerService.produceAnswer(startMenu);
            return;
        }
        sendAnswer(output, chatId);
    }

    private void processLoginStep(AppUser appUser, String text, Long chatId) {
        if (appUser.getPassword() != null && appUser.getPassword().equals(text)) {
            appUser.setState(UserState.IN_MAIN_MENU); // Встановлюємо новий стейт
            appUserDAO.save(appUser);
            sendMainMenu(appUser, chatId);
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
                sendRoleKeyboard(chatId);
                break;

            case WAIT_FOR_ROLE_STATE:
                if (BTN_VOLUNTEER.equalsIgnoreCase(text) || BTN_VICTIM.equalsIgnoreCase(text)) {
                    String role = BTN_VOLUNTEER.equalsIgnoreCase(text) ? "VOLUNTEER" : "VICTIM";
                    appUser.setRole(role);
                    appUser.setState(UserState.WAIT_FOR_PHONE_STATE);
                    appUserDAO.save(appUser);
                    sendAnswerAndRemoveKeyboard("Введіть номер телефону у форматі +380XXXXXXXXX:", chatId);
                } else {
                    sendRoleKeyboard(chatId);
                }
                break;

            case WAIT_FOR_PHONE_STATE:
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
                sendActivationCheckButton(chatId);
                break;

            default:
                sendAnswer("Сталася невідома помилка під час реєстрації. Введіть /cancel і спробуйте ще раз.", chatId);
        }
    }

    private void sendMainMenu(AppUser appUser, Long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow faqRow = new KeyboardRow();
        faqRow.add(BTN_FAQ);

        if ("VOLUNTEER".equalsIgnoreCase(appUser.getRole())) {
            row1.add(BTN_CARDS);
            row1.add(BTN_PROFILE);
            row2.add(BTN_SELECTED_TASKS);
            row2.add(BTN_GATHERINGS);
            keyboardMarkup.setKeyboard(List.of(row1, row2, faqRow));
        } else {
            row1.add(BTN_CREATE_CARD);
            row1.add(BTN_PROFILE);
            row2.add(BTN_MY_CARDS);
            keyboardMarkup.setKeyboard(List.of(row1, row2, faqRow));
        }

        String displayRole = "VOLUNTEER"
                .equalsIgnoreCase(appUser.getRole()) ? BTN_VOLUNTEER : "Заявник";
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Головне меню (" + displayRole + "):");
        message.setReplyMarkup(keyboardMarkup);

        producerService.produceAnswer(message);
    }

    private void processMainMenu(AppUser appUser, String text, Long chatId) {
        String trimmedText = text.trim();

        // --- Меню редагування профілю ---
        if ("Додати фото".equalsIgnoreCase(trimmedText)) {
            editActionMap.put(appUser.getTelegramUserId(), "WAIT_FOR_PROFILE_PHOTO");
            sendAnswer("Надішліть ваше фото профілю:", chatId);
            return;
        }
        if ("Змінити нікнейм".equalsIgnoreCase(trimmedText)) {
            editActionMap.put(appUser.getTelegramUserId(), "NICKNAME");
            sendAnswer("Введіть новий нікнейм:", chatId);
            return;
        }
        if ("Змінити телефон".equalsIgnoreCase(trimmedText)) {
            editActionMap.put(appUser.getTelegramUserId(), "PHONE");
            sendAnswer("Введіть новий телефон у форматі +380XXXXXXXXX:", chatId);
            return;
        }
        if ("Скасувати редагування".equalsIgnoreCase(trimmedText)) {
            sendAnswer("Редагування профілю скасовано.", chatId);
            String profileText = buildProfileInfo(appUser);
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText(profileText);
            msg.setReplyMarkup(buildProfileKeyboard());
            producerService.produceAnswer(msg);
            return;
        }

        // --- Головне меню ---
        if (BTN_CARDS.equalsIgnoreCase(trimmedText)) {
            sendAnswer("Тут буде список карток для волонтера.", chatId);
            return;
        }
        if (BTN_PROFILE.equalsIgnoreCase(trimmedText)) {
            String profileText = buildProfileInfo(appUser);
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText(profileText);
            msg.setReplyMarkup(buildProfileKeyboard());
            producerService.produceAnswer(msg);
            return;
        }
        if ("Забув пароль".equalsIgnoreCase(trimmedText)) {
            sendAnswer("Ось ваш пароль: " + appUser.getPassword() + "\nАле нікому не показуйте і не губіть!", chatId);
            return;
        }
        if ("Редагувати".equalsIgnoreCase(trimmedText)) {
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText("Меню редагування профілю:");
            msg.setReplyMarkup(buildEditProfileKeyboard());
            producerService.produceAnswer(msg);
            return;
        }
        if ("Меню".equalsIgnoreCase(trimmedText)) {
            sendMainMenu(appUser, chatId);
            return;
        }
        if (BTN_SELECTED_TASKS.equalsIgnoreCase(trimmedText)) {
            sendAnswer("Тут буде перелік ваших обраних завдань.", chatId);
            return;
        }
        if (BTN_GATHERINGS.equalsIgnoreCase(trimmedText)) {
            sendAnswer("Тут буде інформація про збори.", chatId);
            return;
        }
        if (BTN_CREATE_CARD.equalsIgnoreCase(trimmedText)) {
            sendAnswer("Тут можна створити нову картку.", chatId);
            return;
        }
        if (BTN_MY_CARDS.equalsIgnoreCase(trimmedText)) {
            sendAnswer("Тут буде список ваших карток.", chatId);
            return;
        }
        if (BTN_FAQ.equalsIgnoreCase(trimmedText)) {
            String faqText = "ℹ️ Часті поради користувачам:\n\n"
                    + "🔹 1. Якщо бот не відповідає або виникає помилка, спробуйте перезапустити діалог або зверніться до адміністратора.\n"
                    + "🔹 2. Введіть команду /cancel для виходу у BASIC_STATE (початковий режим).\n"
                    + "🔹 3. Якщо у вас залишились питання або потрібна допомога — напишіть адміністратору, Валентин вам допоможе.";
            sendAnswer(faqText, chatId);
            return;
        }

        // Якщо нічого не співпало, показуємо повідомлення і головне меню
        sendAnswer("Оберіть дію з меню нижче.", chatId);
        sendMainMenu(appUser, chatId);
    }

    private void sendAnswerAndRemoveKeyboard(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        producerService.produceAnswer(sendMessage);
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

        producerService.produceAnswer(sendMessage);
    }
    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
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

        producerService.produceAnswer(sendMessage);
    }

    private void sendOnlyLoginButton(Long chatId, String message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("Увійти");

        keyboardMarkup.setKeyboard(List.of(row));

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setReplyMarkup(keyboardMarkup);

        producerService.produceAnswer(sendMessage);
    }

    private void sendActivationCheckButton(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(
                "Лист із посиланням для підтвердження реєстрації надіслано на вашу електронну пошту.\n" +
                        "Перейдіть за посиланням у листі, щоб завершити реєстрацію.\n\n" +
                        "Після цього натисніть кнопку нижче."
        );

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add(BTN_CHECK_ACTIVATION);

        keyboardMarkup.setKeyboard(List.of(row));
        sendMessage.setReplyMarkup(keyboardMarkup);

        producerService.produceAnswer(sendMessage);
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
            appUser.setState(UserState.WAIT_FOR_LOGIN_PASSWORD_STATE);
            appUserDAO.save(appUser);
            String username = appUser.getUsername() != null ? appUser.getUsername() : "користувач";
            sendAnswerAndRemoveKeyboard(
                    String.format("З поверненням, %s! Для входу введіть ваш пароль:", username),
                    appUser.getTelegramUserId()
            );
            return null;
        } else if ("Зареєструватися".equalsIgnoreCase(text) || REGISTRATION.equals(serviceCommands)) {
            log.info("isActive: " + appUser.getIsActive() + ", email: " + appUser.getEmail());
            String regResult = appUserService.registerUser(appUser);
            if ("Ви вже зареєстровані, увійдіть в ваш акаунт.".equals(regResult)) {
                sendOnlyLoginButton(
                        appUser.getTelegramUserId(),
                        regResult
                );
            } else {
                sendAnswerAndRemoveKeyboard(regResult, appUser.getTelegramUserId());
            }
            return null;
        } else if (HELP.equals(serviceCommands)) {
            return help();
        } else if (START.equals(serviceCommands)) {
            appUser.setState(BASIC_STATE);
            appUserDAO.save(appUser);
            return "__SHOW_START_MENU__";
        } else {
            return "Невідома команда! Перегляньте список доступних команд /help";
        }
    }

    private void sendRoleKeyboard(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Оберіть роль: '" + BTN_VOLUNTEER + "' або '" + BTN_VICTIM + "'");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add(BTN_VOLUNTEER);
        row.add(BTN_VICTIM);

        keyboardMarkup.setKeyboard(List.of(row));
        sendMessage.setReplyMarkup(keyboardMarkup);

        producerService.produceAnswer(sendMessage);
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

    private String buildProfileInfo(AppUser user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ваш профіль:\n\n");
        sb.append("👤 Нікнейм: ").append(user.getUsername()).append("\n");
        sb.append("📧 Email: ").append(user.getEmail()).append("\n");
        sb.append("📱 Телефон: ").append(user.getPhoneNumber()).append("\n");
        sb.append("🎭 Роль: ").append("VOLUNTEER".equalsIgnoreCase(user.getRole()) ? "Волонтер" : "Заявник").append("\n");
        return sb.toString();
    }

    private ReplyKeyboardMarkup buildProfileKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Редагувати");
        row1.add("Меню");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Забув пароль");

        keyboardMarkup.setKeyboard(List.of(row1, row2));
        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup buildEditProfileKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Додати фото");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Змінити нікнейм");
        row2.add("Змінити телефон");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Скасувати редагування");

        keyboardMarkup.setKeyboard(List.of(row1, row2, row3));
        return keyboardMarkup;
    }

    private void saveRowData(Update update) {
        RawData rawData = RawData.builder().event(update).build();
        rawDataDAO.save(rawData);
    }
}
