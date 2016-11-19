package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.model.EmailVerification;
import com.codeaim.urlcheck.api.model.User;

import java.util.Optional;

public interface IUserRepository
{
    Optional<User> getUserByUsername(String username);

    User createUser(User user);

    boolean verifyEmail(String username, String emailVerificationToken);

    Optional<EmailVerification> getEmailVerificationByUsername(String username);
}
