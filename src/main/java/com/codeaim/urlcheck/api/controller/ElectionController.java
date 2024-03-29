package com.codeaim.urlcheck.api.controller;

import com.codeaim.urlcheck.api.model.Check;
import com.codeaim.urlcheck.api.model.Expire;
import com.codeaim.urlcheck.api.model.Probe;
import com.codeaim.urlcheck.api.model.Result;
import com.codeaim.urlcheck.api.repository.IProbeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/probe")
@ResponseBody
public class ElectionController
{
    private final IProbeRepository probeRepository;

    public ElectionController
            (
                    IProbeRepository probeRepository
            )
    {
        this.probeRepository = probeRepository;
    }

    @RequestMapping(value = "/candidate", method = RequestMethod.POST)
    public List<Check> getCandidates(@RequestBody Probe probe)
    {
        return probeRepository.getCandidates(probe);
    }

    @RequestMapping(value = "/result", method = RequestMethod.POST)
    public void createResults(@RequestBody ArrayList<Result> results)
    {
        probeRepository.createResults(results);
    }

    @RequestMapping(value = "/expire", method = RequestMethod.POST)
    public void expireResults(@RequestBody Expire expire)
    {
        probeRepository.expireResults(expire);
    }
}
