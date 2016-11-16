package com.codeaim.urlcheck.api.controller;

import com.codeaim.urlcheck.api.model.Check;
import com.codeaim.urlcheck.api.model.User;
import com.codeaim.urlcheck.api.repository.ICheckRepository;
import com.codeaim.urlcheck.api.repository.IUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Controller
@RequestMapping("/check")
@ResponseBody
public class CheckController
{
    private final ICheckRepository checkRepository;
    private final IUserRepository userRepository;

    public CheckController(
            ICheckRepository checkRepository,
            IUserRepository userRepository
    )
    {
        this.checkRepository = checkRepository;
        this.userRepository = userRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Check> getChecks()
    {
        return checkRepository.getChecks();
    }

    @RequestMapping(value = "/{username:.+}", method = RequestMethod.GET)
    public List<Check> getChecksByUsername(
            @PathVariable(value = "username")
                    String username
    )
    {
        return checkRepository.getChecks(username);
    }

    @RequestMapping(value = "/{username:.+}", method = RequestMethod.POST)
    public ResponseEntity<?> createCheck(
            @PathVariable(value = "username")
                    String username,
            @RequestBody
            @Valid
                    Check check,
            BindingResult bindingResult
    )
    {
        if(bindingResult.hasErrors())
            return ResponseEntity
                    .unprocessableEntity()
                    .build();

        Optional<User> user = userRepository.getUserByUsername(username);

        if(user.isPresent())
        {
            Check createdCheck = checkRepository
                    .createCheck(check.setUserId(user.get().getId()));
            return ResponseEntity
                    .created(linkTo(methodOn(CheckController.class).getCheckById(user.get().getUsername(), createdCheck.getId())).toUri())
                    .body(createdCheck);
        }
        return ResponseEntity
                        .notFound()
                        .build();
    }

    @RequestMapping(value = "/{username:.+}/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getCheckById(
            @PathVariable(value = "username")
                    String username,
            @PathVariable(value = "id")
                    long id
    )
    {
        Optional<Check> check = checkRepository
                .getChecks(username)
                .stream()
                .filter(x -> x.getId() == id)
                .findFirst();

        return check.isPresent() ? ResponseEntity
                .ok()
                .body(check.get()) : ResponseEntity
            .notFound()
            .build();
    }

    @RequestMapping(value = "/{username:.*}/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> updateCheck(
            @PathVariable(value = "username")
            String username,
            @RequestBody
            @Valid
                    Check check,
            BindingResult bindingResult
    )
    {
        if (bindingResult.hasErrors())
            return ResponseEntity
                    .unprocessableEntity()
                    .build();

        Optional<User> user = userRepository
                .getUserByUsername(username);

        if(user.isPresent() && checkRepository.checkExists(check.getId()))
        {
            Check updatedCheck = checkRepository
                    .updateCheck(check);

            return ResponseEntity
                    .ok()
                    .body(updatedCheck);
        }

        return ResponseEntity
                .notFound()
                .build();
    }

    @RequestMapping(value = "/{username:.+}/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCheckById(
            @PathVariable(value = "username")
                    String username,
            @PathVariable(value = "id")
                    long id
    )
    {
        Optional<Check> check = checkRepository
                .getChecks(username)
                .stream()
                .filter(x -> x.getId() == id)
                .findFirst();

        if(check.isPresent()) {
            checkRepository.deleteCheck(id);

            return ResponseEntity
                    .noContent()
                    .build();
        }

        return ResponseEntity
                .notFound()
                .build();
    }
}
