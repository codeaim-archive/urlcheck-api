package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.model.Check;
import com.codeaim.urlcheck.api.model.Expire;
import com.codeaim.urlcheck.api.model.Probe;
import com.codeaim.urlcheck.api.model.Result;

import java.util.ArrayList;
import java.util.List;

public interface IProbeRepository
{
    List<Check> getCandidates(Probe probe);
    void createResults(ArrayList<Result> results);
    void expireResults(Expire expire);
}
