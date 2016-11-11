package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.model.User;

import java.util.Optional;

public interface IUserRepository
{
    Optional<User> getUserByUsername(String username);
}