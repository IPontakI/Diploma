package diploma.university.service.impl;

import diploma.university.dao.AppUserDAO;
import diploma.university.dao.HelpCardDAO;
import diploma.university.dao.RawDataDAO;
import diploma.university.entity.*;
import diploma.university.entity.enums.CardImportance;
import diploma.university.entity.enums.UserState;
import diploma.university.exeptions.UploadFileException;
import diploma.university.service.*;
import diploma.university.service.enums.ServiceCommands;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final HelpCardDAO helpCardDAO;

    private final Map<Long, String> editActionMap = new ConcurrentHashMap<>();
    private final Map<Long, CardCreationSession> cardSessionMap = new ConcurrentHashMap<>();


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


    public MainServiceImpl(RawDataDAO rawDataDAOl, ProducerService producerService, AppUserDAO appUserDAO, FileService fileService, AppUserService appUserService, HelpCardDAO helpCardDAO) {
        this.rawDataDAO = rawDataDAOl;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.fileService = fileService;
        this.appUserService = appUserService;
        this.helpCardDAO = helpCardDAO;
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

        if ("Меню".equalsIgnoreCase(text.trim())) {
            appUser.setState(UserState.IN_MAIN_MENU);
            appUserDAO.save(appUser);
            sendMainMenu(appUser, chatId);
            return;
        }

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
        if (userState == UserState.CREATE_CARD_TITLE) {
            editActionMap.put(appUser.getTelegramUserId(), text); // Зберігаємо назву
            appUser.setState(UserState.CREATE_CARD_DESCRIPTION);
            appUserDAO.save(appUser);
            sendAnswer("Опишіть проблему/запит:", chatId);
            return;
        }

        if (userState == UserState.CREATE_CARD_DESCRIPTION) {
            String title = editActionMap.get(appUser.getTelegramUserId());
            CardCreationSession session = cardSessionMap.getOrDefault(appUser.getTelegramUserId(), new CardCreationSession());
            session.setTitle(title);
            session.setDescription(text);
            cardSessionMap.put(appUser.getTelegramUserId(), session);

            appUser.setState(UserState.CREATE_CARD_CONTACT);
            appUserDAO.save(appUser);
            sendAnswer("Вкажіть контактну інформацію:", chatId);
            return;
        }

        if (userState == UserState.CREATE_CARD_CONTACT) {
            CardCreationSession session = cardSessionMap.get(appUser.getTelegramUserId());
            session.setContact(text);
            cardSessionMap.put(appUser.getTelegramUserId(), session);

            appUser.setState(UserState.CREATE_CARD_IMPORTANCE);
            appUserDAO.save(appUser);

            SendMessage impMsg = new SendMessage();
            impMsg.setChatId(chatId);
            impMsg.setText("Оберіть важливість картки:");
            impMsg.setReplyMarkup(buildImportanceKeyboard());
            producerService.produceAnswer(impMsg);
            return;
        }

        if (userState == UserState.CREATE_CARD_IMPORTANCE) {
            String input = text.trim().toUpperCase();
            if (!input.equals("LOW") && !input.equals("MEDIUM") && !input.equals("HIGH")) {
                SendMessage impMsg = new SendMessage();
                impMsg.setChatId(chatId);
                impMsg.setText("❗️ Виберіть важливість, натиснувши одну з кнопок нижче:");
                impMsg.setReplyMarkup(buildImportanceKeyboard());
                producerService.produceAnswer(impMsg);
                return;
            }

            CardCreationSession session = cardSessionMap.get(appUser.getTelegramUserId());
            session.setImportance(CardImportance.valueOf(input));

            HelpCard card = new HelpCard();
            card.setTitle(session.getTitle());
            card.setDescription(session.getDescription());
            card.setContact(session.getContact());
            card.setImportance(session.getImportance());
            card.setCreator(appUser);
            card.setStatus("Відкрита");
            helpCardDAO.save(card);

            SendMessage doneMsg = new SendMessage();
            doneMsg.setChatId(chatId);
            doneMsg.setText("Картку успішно створено!");
            doneMsg.setReplyMarkup(new ReplyKeyboardRemove(true));
            producerService.produceAnswer(doneMsg);

            showCreatedCard(card, chatId);

            cardSessionMap.remove(appUser.getTelegramUserId());
            appUser.setState(UserState.IN_MAIN_MENU);
            appUserDAO.save(appUser);

            sendMainMenu(appUser, chatId);
            return;
        }

        if (userState == UserState.VIEW_CARDS_LIST) {
            try {
                Long cardId = Long.parseLong(text.trim());
                Optional<HelpCard> cardOpt = helpCardDAO.findByIdAndIsDeletedFalse(cardId);
                if (cardOpt.isPresent() && "Відкрита".equals(cardOpt.get().getStatus())) {
                    showVolunteerCardDetails(cardOpt.get(), chatId, appUser);
                    appUser.setState(UserState.VIEW_CARD_DETAILS);
                    appUserDAO.save(appUser);
                } else {
                    sendAnswer("Картку з таким id не знайдено або вона вже взята.", chatId);
                }
            } catch (NumberFormatException ex) {
                sendAnswer("Введіть лише id картки (наприклад: 3).", chatId);
            }
            return;
        }

        if (userState == UserState.VIEW_CARD_DETAILS) {
            String lastCardIdStr = editActionMap.get(appUser.getTelegramUserId());
            Long lastCardId = lastCardIdStr != null ? Long.parseLong(lastCardIdStr) : null;

            if ("Назад".equalsIgnoreCase(text.trim())) {
                showAllCardsForVolunteer(chatId, appUser);
                appUser.setState(UserState.VIEW_CARDS_LIST);
                appUserDAO.save(appUser);
                return;
            }

            if ("Взяти картку".equalsIgnoreCase(text.trim()) && lastCardId != null) {
                Optional<HelpCard> cardOpt = helpCardDAO.findById(lastCardId);
                if (cardOpt.isPresent() && "Відкрита".equals(cardOpt.get().getStatus())) {
                    HelpCard card = cardOpt.get();
                    card.setStatus("Виконується");
                    card.setVolunteer(appUser);
                    helpCardDAO.save(card);
                    sendAnswer("Ви взяли картку. Дякуємо за вашу допомогу!", chatId);

                    showAllCardsForVolunteer(chatId, appUser);
                    appUser.setState(UserState.VIEW_CARDS_LIST);
                    appUserDAO.save(appUser);
                } else {
                    sendAnswer("Картку вже взяв хтось інший або вона недоступна.", chatId);
                    showAllCardsForVolunteer(chatId, appUser);
                    appUser.setState(UserState.VIEW_CARDS_LIST);
                    appUserDAO.save(appUser);
                }
                editActionMap.remove(appUser.getTelegramUserId());
                return;
            }
            sendAnswer("Оберіть дію з кнопок нижче.", chatId);
            return;
        }

        if (userState == UserState.VIEW_TAKEN_CARDS_LIST) {
            // Отримуємо список id карток, які виводились
            String cardIdsStr = editActionMap.get(appUser.getTelegramUserId());
            if (cardIdsStr == null) {
                sendAnswer("Щось пішло не так. Спробуйте ще раз.", chatId);
                return;
            }
            List<Long> cardIds = Arrays.stream(cardIdsStr.replaceAll("[\\[\\] ]", "").split(","))
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            try {
                int chosenNumber = Integer.parseInt(text.trim());
                if (chosenNumber < 1 || chosenNumber > cardIds.size()) {
                    sendAnswer("Введіть коректний номер картки.", chatId);
                    return;
                }
                Long chosenCardId = cardIds.get(chosenNumber - 1);
                Optional<HelpCard> cardOpt = helpCardDAO.findById(chosenCardId);
                if (cardOpt.isPresent() && "Виконується".equals(cardOpt.get().getStatus())
                        && appUser.equals(cardOpt.get().getVolunteer())) {
                    showVolunteerActiveCardDetails(cardOpt.get(), chatId, appUser);
                    appUser.setState(UserState.VIEW_TAKEN_CARD_DETAILS);
                    appUserDAO.save(appUser);
                    editActionMap.put(appUser.getTelegramUserId(), chosenCardId.toString());
                } else {
                    sendAnswer("Картку не знайдено або вона вже неактивна.", chatId);
                }
            } catch (NumberFormatException e) {
                sendAnswer("Введіть лише номер картки, наприклад: 1", chatId);
            }
            return;
        }

        if (userState == UserState.VIEW_TAKEN_CARD_DETAILS) {
            String lastCardIdStr = editActionMap.get(appUser.getTelegramUserId());
            Long lastCardId = lastCardIdStr != null ? Long.parseLong(lastCardIdStr) : null;

            if ("Назад".equalsIgnoreCase(text.trim())) {
                showVolunteerTakenCards(appUser, chatId);
                appUser.setState(UserState.VIEW_TAKEN_CARDS_LIST);
                appUserDAO.save(appUser);
                return;
            }
            if ("Завершити".equalsIgnoreCase(text.trim()) && lastCardId != null) {
                Optional<HelpCard> cardOpt = helpCardDAO.findById(lastCardId);
                if (cardOpt.isPresent() && appUser.equals(cardOpt.get().getVolunteer())) {
                    HelpCard card = cardOpt.get();
                    card.setStatus("Неактивна");
                    helpCardDAO.save(card);
                    sendAnswer("Завдання завершене!", chatId);
                    showVolunteerTakenCards(appUser, chatId);
                    appUser.setState(UserState.VIEW_TAKEN_CARDS_LIST);
                    appUserDAO.save(appUser);
                } else {
                    sendAnswer("Картку не знайдено або вона вже неактивна.", chatId);
                }
                editActionMap.remove(appUser.getTelegramUserId());
                return;
            }
            if ("Відмінити".equalsIgnoreCase(text.trim()) && lastCardId != null) {
                Optional<HelpCard> cardOpt = helpCardDAO.findById(lastCardId);
                if (cardOpt.isPresent() && appUser.equals(cardOpt.get().getVolunteer())) {
                    HelpCard card = cardOpt.get();
                    card.setStatus("Відкрита");
                    card.setVolunteer(null); // звільняємо картку
                    helpCardDAO.save(card);
                    sendAnswer("Завдання повернено у відкриті!", chatId);
                    showVolunteerTakenCards(appUser, chatId);
                    appUser.setState(UserState.VIEW_TAKEN_CARDS_LIST);
                    appUserDAO.save(appUser);
                } else {
                    sendAnswer("Картку не знайдено або вона вже неактивна.", chatId);
                }
                editActionMap.remove(appUser.getTelegramUserId());
                return;
            }
            sendAnswer("Оберіть дію з кнопок нижче.", chatId);
            return;
        }

        if (userState == UserState.VIEW_OWN_CARDS_LIST) {
            try {
                Long cardId = Long.parseLong(text.trim());
                Optional<HelpCard> cardOpt = helpCardDAO.findByIdAndIsDeletedFalse(cardId);
                if (cardOpt.isPresent() && cardOpt.get().getCreator().getId().equals(appUser.getId())) {
                    showOwnCardDetails(cardOpt.get(), chatId, appUser);
                    appUser.setState(UserState.VIEW_OWN_CARD_DETAILS);
                    appUserDAO.save(appUser);
                } else {
                    sendAnswer("Картку з таким id не знайдено або вона не ваша.", chatId);
                }
            } catch (NumberFormatException ex) {
                sendAnswer("Введіть лише id картки (наприклад: 12).", chatId);
            }
            return;
        }

        if (userState == UserState.VIEW_OWN_CARD_DETAILS) {
            String lastCardIdStr = editActionMap.get(appUser.getTelegramUserId());
            Long lastCardId = lastCardIdStr != null ? Long.parseLong(lastCardIdStr) : null;

            if ("Назад".equalsIgnoreCase(text.trim())) {
                showMyCards(appUser, chatId);
                appUser.setState(UserState.VIEW_OWN_CARDS_LIST);
                appUserDAO.save(appUser);
                return;
            }
            if ("Видалити".equalsIgnoreCase(text.trim()) && lastCardId != null) {
                Optional<HelpCard> cardOpt = helpCardDAO.findById(lastCardId);
                if (cardOpt.isPresent()) {
                    HelpCard card = cardOpt.get();
                    card.setIsDeleted(true);
                    helpCardDAO.save(card);
                    sendAnswer("Картку видалено.", chatId);
                } else {
                    sendAnswer("Картку не знайдено.", chatId);
                }
                showMyCards(appUser, chatId);
                appUser.setState(UserState.VIEW_OWN_CARDS_LIST);
                appUserDAO.save(appUser);
                editActionMap.remove(appUser.getTelegramUserId());
                return;
            }
            if ("Редагувати".equalsIgnoreCase(text.trim()) && lastCardId != null) {
                SendMessage editMenuMsg = new SendMessage();
                editMenuMsg.setChatId(chatId);
                editMenuMsg.setText("Що саме бажаєте змінити?");
                editMenuMsg.setReplyMarkup(buildEditCardKeyboard());
                producerService.produceAnswer(editMenuMsg);

                editActionMap.put(appUser.getTelegramUserId(), lastCardIdStr);
                appUser.setState(UserState.EDIT_CARD_MENU);
                appUserDAO.save(appUser);
                return;
            }
            sendAnswer("Оберіть дію з кнопок нижче.", chatId);
            return;
        }

        if (userState == UserState.EDIT_CARD_MENU) {
            String lastCardIdStr = editActionMap.get(appUser.getTelegramUserId());
            Long lastCardId = lastCardIdStr != null ? Long.parseLong(lastCardIdStr) : null;

            if ("Назва".equalsIgnoreCase(text.trim())) {
                sendAnswer("Введіть нову назву картки:", chatId);
                appUser.setState(UserState.EDIT_CARD_TITLE);
                appUserDAO.save(appUser);
                return;
            }
            if ("Опис".equalsIgnoreCase(text.trim())) {
                sendAnswer("Введіть новий опис картки:", chatId);
                appUser.setState(UserState.EDIT_CARD_DESCRIPTION);
                appUserDAO.save(appUser);
                return;
            }
            if ("Контакти".equalsIgnoreCase(text.trim())) {
                sendAnswer("Введіть нові контакти:", chatId);
                appUser.setState(UserState.EDIT_CARD_CONTACT);
                appUserDAO.save(appUser);
                return;
            }
            if ("Важливість".equalsIgnoreCase(text.trim())) {
                SendMessage impMsg = new SendMessage();
                impMsg.setChatId(chatId);
                impMsg.setText("Оберіть нову важливість картки:");
                impMsg.setReplyMarkup(buildImportanceKeyboard());
                producerService.produceAnswer(impMsg);
                appUser.setState(UserState.EDIT_CARD_IMPORTANCE);
                appUserDAO.save(appUser);
                return;
            }
            if ("Назад".equalsIgnoreCase(text.trim())) {
                // Повертаємось до перегляду картки
                Optional<HelpCard> cardOpt = helpCardDAO.findById(lastCardId);
                if (cardOpt.isPresent()) {
                    showOwnCardDetails(cardOpt.get(), chatId, appUser);
                    appUser.setState(UserState.VIEW_OWN_CARD_DETAILS);
                    appUserDAO.save(appUser);
                }
                return;
            }
            sendAnswer("Оберіть дію з меню нижче.", chatId);
            return;
        }

        if (userState == UserState.EDIT_CARD_TITLE) {
            String lastCardIdStr = editActionMap.get(appUser.getTelegramUserId());
            Long lastCardId = lastCardIdStr != null ? Long.parseLong(lastCardIdStr) : null;
            Optional<HelpCard> cardOpt = helpCardDAO.findById(lastCardId);
            if (cardOpt.isPresent()) {
                HelpCard card = cardOpt.get();
                card.setTitle(text);
                helpCardDAO.save(card);
                sendAnswer("Назву картки оновлено.", chatId);
            }
            // Повертаємось у меню редагування
            appUser.setState(UserState.EDIT_CARD_MENU);
            appUserDAO.save(appUser);
            SendMessage editMenuMsg = new SendMessage();
            editMenuMsg.setChatId(chatId);
            editMenuMsg.setText("Що ще бажаєте змінити?");
            editMenuMsg.setReplyMarkup(buildEditCardKeyboard());
            producerService.produceAnswer(editMenuMsg);
            return;
        }
        if (userState == UserState.EDIT_CARD_DESCRIPTION) {
            String lastCardIdStr = editActionMap.get(appUser.getTelegramUserId());
            Long lastCardId = lastCardIdStr != null ? Long.parseLong(lastCardIdStr) : null;
            Optional<HelpCard> cardOpt = helpCardDAO.findById(lastCardId);
            if (cardOpt.isPresent()) {
                HelpCard card = cardOpt.get();
                card.setDescription(text);
                helpCardDAO.save(card);
                sendAnswer("Опис картки оновлено.", chatId);
            }
            appUser.setState(UserState.EDIT_CARD_MENU);
            appUserDAO.save(appUser);
            SendMessage editMenuMsg = new SendMessage();
            editMenuMsg.setChatId(chatId);
            editMenuMsg.setText("Що ще бажаєте змінити?");
            editMenuMsg.setReplyMarkup(buildEditCardKeyboard());
            producerService.produceAnswer(editMenuMsg);
            return;
        }
        if (userState == UserState.EDIT_CARD_CONTACT) {
            String lastCardIdStr = editActionMap.get(appUser.getTelegramUserId());
            Long lastCardId = lastCardIdStr != null ? Long.parseLong(lastCardIdStr) : null;
            Optional<HelpCard> cardOpt = helpCardDAO.findById(lastCardId);
            if (cardOpt.isPresent()) {
                HelpCard card = cardOpt.get();
                card.setContact(text);
                helpCardDAO.save(card);
                sendAnswer("Контакти картки оновлено.", chatId);
            }
            appUser.setState(UserState.EDIT_CARD_MENU);
            appUserDAO.save(appUser);
            SendMessage editMenuMsg = new SendMessage();
            editMenuMsg.setChatId(chatId);
            editMenuMsg.setText("Що ще бажаєте змінити?");
            editMenuMsg.setReplyMarkup(buildEditCardKeyboard());
            producerService.produceAnswer(editMenuMsg);
            return;
        }
        if (userState == UserState.EDIT_CARD_IMPORTANCE) {
            String input = text.trim().toUpperCase();
            if (!input.equals("LOW") && !input.equals("MEDIUM") && !input.equals("HIGH")) {
                SendMessage impMsg = new SendMessage();
                impMsg.setChatId(chatId);
                impMsg.setText("❗️ Виберіть важливість, натиснувши одну з кнопок нижче:");
                impMsg.setReplyMarkup(buildImportanceKeyboard());
                producerService.produceAnswer(impMsg);
                return;
            }
            String lastCardIdStr = editActionMap.get(appUser.getTelegramUserId());
            Long lastCardId = lastCardIdStr != null ? Long.parseLong(lastCardIdStr) : null;
            Optional<HelpCard> cardOpt = helpCardDAO.findById(lastCardId);
            if (cardOpt.isPresent()) {
                HelpCard card = cardOpt.get();
                card.setImportance(CardImportance.valueOf(input));
                helpCardDAO.save(card);
                sendAnswer("Важливість картки оновлено.", chatId);
            }
            appUser.setState(UserState.EDIT_CARD_MENU);
            appUserDAO.save(appUser);
            SendMessage editMenuMsg = new SendMessage();
            editMenuMsg.setChatId(chatId);
            editMenuMsg.setText("Що ще бажаєте змінити?");
            editMenuMsg.setReplyMarkup(buildEditCardKeyboard());
            producerService.produceAnswer(editMenuMsg);
            return;
        }
        
        if (editActionMap.containsKey(userId) && "WAIT_FOR_PROFILE_PHOTO".equals(editActionMap.get(userId))) {
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
                sendActivationCheckButton(chatId);
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

    private void showOwnCardDetails(HelpCard card, Long chatId, AppUser user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ваша картка:\n");
        sb.append("ID: ").append(card.getId()).append("\n");
        sb.append("Назва: ").append(card.getTitle()).append("\n");
        sb.append("Опис: ").append(card.getDescription()).append("\n");
        sb.append("Контакт: ").append(card.getContact()).append("\n");
        sb.append("Важливість: ").append(card.getImportance()).append("\n");
        sb.append("Статус: ").append(card.getStatus()).append("\n");
        if (card.getVolunteer() != null) {
            sb.append("Волонтер: ").append(card.getVolunteer().getUsername()).append("\n");
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());
        message.setReplyMarkup(buildOwnCardActionKeyboard());
        producerService.produceAnswer(message);

        editActionMap.put(user.getTelegramUserId(), card.getId().toString());
    }

    private ReplyKeyboardMarkup buildOwnCardActionKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Редагувати");
        row1.add("Видалити");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Назад");

        keyboardMarkup.setKeyboard(List.of(row1, row2));
        return keyboardMarkup;
    }

    private void showVolunteerActiveCardDetails(HelpCard card, Long chatId, AppUser user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Картка:\n");
        sb.append("Назва: ").append(card.getTitle()).append("\n");
        sb.append("Опис: ").append(card.getDescription()).append("\n");
        sb.append("Контакт: ").append(card.getContact()).append("\n");
        sb.append("Важливість: ").append(card.getImportance()).append("\n");
        sb.append("Статус: ").append(card.getStatus()).append("\n");

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());
        message.setReplyMarkup(buildVolunteerTakenCardActionKeyboard());
        producerService.produceAnswer(message);
    }
    private ReplyKeyboardMarkup buildVolunteerTakenCardActionKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("Завершити");
        row.add("Відмінити");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Назад");
        keyboardMarkup.setKeyboard(List.of(row, row2));
        return keyboardMarkup;
    }

    private void showVolunteerCardDetails(HelpCard card, Long chatId, AppUser user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Картка:\n");
        sb.append("Назва: ").append(card.getTitle()).append("\n");
        sb.append("Опис: ").append(card.getDescription()).append("\n");
        sb.append("Контакт: ").append(card.getContact()).append("\n");
        sb.append("Важливість: ").append(card.getImportance()).append("\n");
        sb.append("Статус: ").append(card.getStatus()).append("\n");

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());
        message.setReplyMarkup(buildVolunteerCardActionKeyboard());
        producerService.produceAnswer(message);

        editActionMap.put(user.getTelegramUserId(), card.getId().toString());
    }

    private ReplyKeyboardMarkup buildVolunteerCardActionKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("Взяти картку");
        row.add("Назад");

        keyboardMarkup.setKeyboard(List.of(row));
        return keyboardMarkup;
    }

    private void showCreatedCard(HelpCard card, Long chatId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ваша картка допомоги:\n\n");
        sb.append("Назва: ").append(card.getTitle()).append("\n");
        sb.append("Опис: ").append(card.getDescription()).append("\n");
        sb.append("Контакт: ").append(card.getContact()).append("\n");
        sb.append("Важливість: ").append(card.getImportance()).append("\n");
        sb.append("Статус: ").append(card.getStatus()).append("\n");

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());
        producerService.produceAnswer(message);
    }
    private void showMyCards(AppUser user, Long chatId) {
        List<HelpCard> cards = helpCardDAO.findByCreatorAndIsDeletedFalse(user);

        StringBuilder sb = new StringBuilder();
        if (cards.isEmpty()) {
            sb.append("У вас ще немає створених карток.");
        } else {
            sb.append("Ваші картки:\n\n");
            for (HelpCard card : cards) {
                sb.append(card.getId())
                        .append(". ")
                        .append(card.getTitle())
                        .append(" (Статус: ")
                        .append(card.getStatus())
                        .append(")\n");
            }
            sb.append("\nДля дій з карткою введіть її id (наприклад: 12)");
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());
        message.setReplyMarkup(buildMenuKeyboard());

        producerService.produceAnswer(message);

        user.setState(UserState.VIEW_OWN_CARDS_LIST);
        appUserDAO.save(user);
    }

    private ReplyKeyboardMarkup buildMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("Меню");

        keyboardMarkup.setKeyboard(List.of(row));
        return keyboardMarkup;
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
            row1.add(BTN_PROFILE);
            row2.add(BTN_CARDS);
            row2.add(BTN_SELECTED_TASKS);
            keyboardMarkup.setKeyboard(List.of(row1, row2, faqRow));
        } else {
            row1.add(BTN_PROFILE);
            row2.add(BTN_CREATE_CARD);
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

        if ("Меню".equalsIgnoreCase(text.trim())) {
            appUser.setState(UserState.IN_MAIN_MENU);
            appUserDAO.save(appUser);
            sendMainMenu(appUser, chatId);
            return;
        }

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
            showAllCardsForVolunteer(chatId, appUser);
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
        if (BTN_SELECTED_TASKS.equalsIgnoreCase(trimmedText)) {
            showVolunteerTakenCards(appUser, chatId);
            return;
        }
        if (BTN_GATHERINGS.equalsIgnoreCase(trimmedText)) {
            sendAnswer("Тут буде інформація про збори.", chatId);
            return;
        }
        if (BTN_CREATE_CARD.equalsIgnoreCase(trimmedText)) {
            appUser.setState(UserState.CREATE_CARD_TITLE);
            appUserDAO.save(appUser);
            sendAnswerAndRemoveKeyboard("Введіть назву картки:", chatId);
            cardSessionMap.put(appUser.getTelegramUserId(), new CardCreationSession());
            return;
        }
        if (BTN_MY_CARDS.equalsIgnoreCase(trimmedText)) {
            showMyCards(appUser, chatId);
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

        sendAnswer("Оберіть дію з меню нижче.", chatId);
        sendMainMenu(appUser, chatId);
    }

    private void showVolunteerTakenCards(AppUser user, Long chatId) {
        List<HelpCard> cards = helpCardDAO.findByVolunteerAndStatusAndIsDeletedFalse(user, "Виконується");

        StringBuilder sb = new StringBuilder();
        if (cards.isEmpty()) {
            sb.append("У вас немає взятих завдань.");
        } else {
            sb.append("Ваші взяті картки:\n\n");
            int i = 1;
            for (HelpCard card : cards) {
                sb.append(i).append(". ").append(card.getTitle()).append("\n");
                i++;
            }
            sb.append("\nДля перегляду картки введіть її номер (наприклад: 1)");
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());
        message.setReplyMarkup(buildMenuKeyboard());

        producerService.produceAnswer(message);


        List<Long> cardIds = cards.stream()
                .map(HelpCard::getId).collect(Collectors.toList());
        editActionMap.put(user.getTelegramUserId(), cardIds.toString());
        user.setState(UserState.VIEW_TAKEN_CARDS_LIST);
        appUserDAO.save(user);
    }

    private ReplyKeyboardMarkup buildEditCardKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Назва");
        row1.add("Опис");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Контакти");
        row2.add("Важливість");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Назад");

        keyboardMarkup.setKeyboard(List.of(row1, row2, row3));
        return keyboardMarkup;
    }

    private void sendAnswerAndRemoveKeyboard(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        producerService.produceAnswer(sendMessage);
    }

    private void showAllCardsForVolunteer(Long chatId, AppUser user) {
        List<HelpCard> cards = helpCardDAO.findByStatusAndIsDeletedFalse("Відкрита");

        StringBuilder sb = new StringBuilder();
        if (cards.isEmpty()) {
            sb.append("Поки що немає відкритих карток для допомоги.");
        } else {
            sb.append("Список карток:\n\n");
            for (HelpCard card : cards) {
                sb.append(card.getId())
                        .append(". ")
                        .append(card.getTitle())
                        .append(" (")
                        .append(card.getImportance())
                        .append(")\n");
            }
            sb.append("\nДля перегляду картки введіть її id (наприклад: 3)");
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());
        message.setReplyMarkup(buildMenuKeyboard());

        producerService.produceAnswer(message);

        user.setState(UserState.VIEW_CARDS_LIST);
        appUserDAO.save(user);
    }

    private ReplyKeyboardMarkup buildImportanceKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("LOW");
        row.add("MEDIUM");
        row.add("HIGH");

        keyboardMarkup.setKeyboard(List.of(row));
        return keyboardMarkup;
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
