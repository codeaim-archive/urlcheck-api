package com.codeaim.urlcheck.api.controller;

import com.codeaim.urlcheck.api.model.Check;
import com.codeaim.urlcheck.api.repository.ICheckRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/check")
@ResponseBody
public class CheckController
{
    private final ICheckRepository checkRepository;

    public CheckController(
            ICheckRepository checkRepository
    )
    {
        this.checkRepository = checkRepository;
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
}
