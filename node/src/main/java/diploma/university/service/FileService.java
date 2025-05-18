package diploma.university.service;

import diploma.university.entity.AppDocument;
import diploma.university.entity.AppPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
}
