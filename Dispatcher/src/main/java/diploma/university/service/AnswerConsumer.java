package diploma.university.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface AnswerConsumer {
    void consume(SendMessage sendMessage);
}
