package com.codeaim.urlcheck.api.controller;

import com.codeaim.urlcheck.api.client.FaviconClient;
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
    private final FaviconClient faviconClient;

    public CheckController(
            ICheckRepository checkRepository,
            IUserRepository userRepository,
            FaviconClient faviconClient
    )
    {
        this.checkRepository = checkRepository;
        this.userRepository = userRepository;
        this.faviconClient = faviconClient;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Check> getChecks()
    {
        return checkRepository.getChecksByUsername();
    }

    @RequestMapping(value = "/{username:.+}", method = RequestMethod.GET)
    public List<Check> getChecksByUsername(
            @PathVariable(value = "username")
                    String username,
            @RequestParam(value = "events", required = false)
                    boolean events
    )
    {
        return events ?
                checkRepository.getChecksWithEventsByUsername(username) :
                checkRepository.getChecksByUsername(username);
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
        if (bindingResult.hasErrors())
            return ResponseEntity
                    .unprocessableEntity()
                    .build();

        Optional<User> user = userRepository.getUserByUsername(username);

        if (user.isPresent())
        {
            Check createdCheck = checkRepository
                    .createCheck(check
                            .setUserId(user.get().getId())
                            .setFavicon(faviconClient.getFavicon(check.getUrl())));

            return ResponseEntity
                    .created(linkTo(methodOn(CheckController.class)
                            .getCheckById(username, createdCheck.getId()))
                            .toUri())
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
                .getCheckById(id);

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

        if (user.isPresent() && checkRepository.checkExists(check.getId()))
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
            @PathVariable(value = "id")
                    long id
    )
    {
        Optional<Check> check = checkRepository
                .getCheckById(id);

        if (check.isPresent())
        {
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
