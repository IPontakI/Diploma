package diploma.university.service.impl;

import diploma.university.dao.RawDataDAO;
import diploma.university.entity.RawData;
import diploma.university.service.MainService;
import diploma.university.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;

    public MainServiceImpl(RawDataDAO rawDataDAOl, ProducerService producerService) {
        this.rawDataDAO = rawDataDAOl;
        this.producerService = producerService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRowData(update);

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from NODE");
        producerService.produceAnswear(sendMessage);
    }

    private void saveRowData(Update update) {
        RawData rawData = RawData.builder().event(update).build();
        rawDataDAO.save(rawData);
    }
}
