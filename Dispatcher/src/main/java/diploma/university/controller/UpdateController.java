package diploma.university.controller;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.List;

import diploma.university.service.UpdateProducer;
import diploma.university.utils.MessageUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static diploma.university.model.RabbitQueue.*;

@Component
@Log4j
public class UpdateController {
    private TgBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer){
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }
                                                      
    public void registerBot(TgBot telegramBot){
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update){
        if (update == null){
            log.error("Received update is null");
            return;
        }

        if (update.hasMessage()){
            distributeMessageByType(update);
        }else {
            log.error("Unsupported message type received:" + update);
        }
    }

    private void distributeMessageByType(Update update) {
        var message = update.getMessage();
        if (message.hasText()){
            processTextMessage(update);
        } else if (message.hasDocument()){
            processDocMessage(update);
        }else if (message.hasPhoto()){
            processPhotoMessage(update);
        }else{
            setUnsupportedMessage(update);
        }
    }

    public void setView(SendMessage sendMessage){
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void setFileIsReceivedView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Файл отримано! Обробляємо...");
        setView(sendMessage);
    }

    private void setUnsupportedMessage(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Непідтримуваний тип повідомлення!");
        setView(sendMessage);
    }

    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
//        setFileIsReceivedView(update); //Unused method
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
//        setFileIsReceivedView(update); //Unused method
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
//        setFileIsReceivedView(update); //Unused method

//        String text = update.getMessage().getText();
//        Long chatId = update.getMessage().getChatId();
//
//        if ("/start".equalsIgnoreCase(text)) {
//            SendMessage startMessage = StartMenu(chatId);
//            setView(startMessage);
//            return;
//        }
    }

//    private SendMessage StartMenu(Long chatId) {
//        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//        keyboardMarkup.setResizeKeyboard(true);
//        keyboardMarkup.setOneTimeKeyboard(false); // щоб меню залишалося
//
//        KeyboardRow row = new KeyboardRow();
//        row.add("Зареєструватися");
//        row.add("Увійти");
//
//        keyboardMarkup.setKeyboard(List.of(row));
//
//        SendMessage message = new SendMessage();
//        message.setChatId(chatId);
//        message.setText("Створіть або увійдіть у свій обліковий запис:");
//        message.setReplyMarkup(keyboardMarkup);
//        return message;
//    }
}
