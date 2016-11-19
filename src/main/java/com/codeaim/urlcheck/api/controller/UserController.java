package com.codeaim.urlcheck.api.controller;

import com.codeaim.urlcheck.api.model.User;
import com.codeaim.urlcheck.api.repository.IUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

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

    @RequestMapping(value = "/{username:.+}", method = RequestMethod.GET)
    public ResponseEntity<?> getUserByUsername
            (
                    @PathVariable(value = "username")
                            String username
            )
    {
        Optional<User> user = userRepository
                .getUserByUsername(username);

        return user.isPresent() ?
                ResponseEntity
                        .ok()
                        .body(user.get()) :
                ResponseEntity
                        .notFound()
                        .build();
    }


    @RequestMapping(value = "/{username:.+}/verify", method = RequestMethod.POST)
    public ResponseEntity<?> verifyUserEmail(
            @PathVariable(value = "username")
                    String username,
            @RequestBody
            @Valid
                    String emailVerificationToken,
            BindingResult bindingResult
    )
    {
        if (bindingResult.hasErrors())
            return ResponseEntity
                    .unprocessableEntity()
                    .build();

        return userRepository
                .verifyEmail(
                        username,
                        emailVerificationToken) ?
                ResponseEntity
                        .ok()
                        .build() :
                ResponseEntity
                        .noContent()
                        .build();
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> createUser(
            @RequestBody
            @Valid
                    User user,
            BindingResult bindingResult
    )
    {
        if (bindingResult.hasErrors())
            return ResponseEntity
                    .unprocessableEntity()
                    .build();

        Optional<User> userByUsername = userRepository
                .getUserByUsername(user.getUsername());
        Optional<User> userByEmail = userRepository
                .getUserByUsername(user.getEmail());

        if (userByUsername.isPresent() || userByEmail.isPresent())
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();

        User createdUser = userRepository
                .createUser(user);

        return ResponseEntity
                .created(linkTo(methodOn(UserController.class)
                        .getUserByUsername(user.getUsername()))
                        .toUri())
                .body(createdUser);
    }
}
