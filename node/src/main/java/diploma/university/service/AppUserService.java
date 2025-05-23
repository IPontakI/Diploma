package diploma.university.service;

import diploma.university.entity.AppUser;

public interface AppUserService {
    String registerUser (AppUser appUser);
    String setEmail(AppUser appUser, String mail);
}
