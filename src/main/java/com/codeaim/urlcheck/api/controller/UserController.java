package com.codeaim.urlcheck.api.controller;

import com.codeaim.urlcheck.api.client.EmailClient;
import com.codeaim.urlcheck.api.model.EmailVerification;
import com.codeaim.urlcheck.api.model.User;
import com.codeaim.urlcheck.api.model.Verification;
import com.codeaim.urlcheck.api.model.Verify;
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
    private final EmailClient emailClient;

    public UserController
            (
                    IUserRepository userRepository,
                    EmailClient emailClient
            )
    {
        this.userRepository = userRepository;
        this.emailClient = emailClient;
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

    @RequestMapping(value = "/{username:.+}/verification", method = RequestMethod.POST)
    public ResponseEntity<?> verificationEmail(
            @PathVariable(value = "username")
                    String username,
            @RequestBody
                    Verification verification
    )
    {
        Optional<EmailVerification> emailVerification = userRepository
                .getEmailVerificationByUsername(username);

        if(emailVerification.isPresent())
            return emailClient.sendVerifyEmail(
                    emailVerification.get().getEmail(),
                    emailVerification.get().getUsername(),
                    emailVerification.get().getEmailVerificationToken()) ?
                    ResponseEntity
                            .ok()
                            .build() :
                    ResponseEntity
                            .noContent()
                            .build();

        return ResponseEntity
                .noContent()
                .build();
    }

    @RequestMapping(value = "/{username:.+}/verify", method = RequestMethod.POST)
    public ResponseEntity<?> verifyUserEmail(
            @PathVariable(value = "username")
                    String username,
            @RequestBody
            @Valid
                    Verify verify,
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
                        verify.getEmailVerificationToken()) ?
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
