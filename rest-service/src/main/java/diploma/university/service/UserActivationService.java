package diploma.university.service;

import diploma.university.service.enums.ActivationStatus;

public interface UserActivationService {
    ActivationStatus activation(String cryptoUserId);
}
