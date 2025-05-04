package diploma.university.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface ProducerService {
    void produceAnswear(SendMessage sendMessage);
}
