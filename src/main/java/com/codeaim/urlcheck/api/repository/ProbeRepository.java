package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.model.Check;
import com.codeaim.urlcheck.api.model.Probe;
import com.codeaim.urlcheck.api.model.Result;
import com.codeaim.urlcheck.api.model.Status;
import com.google.common.collect.ImmutableList;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class ProbeRepository implements IProbeRepository
{
    public List<Check> getCandidates(Probe probe) {
        return ImmutableList.of(
                new Check().setId(1).setName("Facebook").setUrl("http://www.facebook.com").setStatus(Status.UP),
                new Check().setId(2).setName("Google").setUrl("http://www.google.com").setStatus(Status.UP),
                new Check().setId(3).setName("Twitter").setUrl("http://www.twitter.com").setStatus(Status.UP),
                new Check().setId(4).setName("Yahoo").setUrl("http://www.yahoo.com").setStatus(Status.DOWN));
    }

    public List<Result> createResults(ArrayList<Result> results) {
        return results.stream().map(x -> x.setId(1)).collect(Collectors.toList());
    }
}
