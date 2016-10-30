package com.codeaim.urlcheck.api.controller;

import com.codeaim.urlcheck.api.model.Check;
import com.codeaim.urlcheck.api.model.User;
import com.codeaim.urlcheck.api.repository.ICheckRepository;
import com.codeaim.urlcheck.api.repository.IUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
@ResponseBody
public class UserController
{
    private final IUserRepository userRepository;

    public UserController
            (
                    IUserRepository userRepository
            )
    {
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.GET)
    public ResponseEntity<?> getUserByUsername
            (
                    @PathVariable(value = "username")
                            String username
            )
    {
        Optional<User> user = userRepository
                .getUserByUsername(username);

        return user.isPresent() ?
                ResponseEntity.ok().body(user.get()) :
                ResponseEntity.notFound().build();
    }
}
