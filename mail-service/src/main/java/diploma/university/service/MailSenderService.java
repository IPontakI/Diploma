package diploma.university.service;

import diploma.university.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
