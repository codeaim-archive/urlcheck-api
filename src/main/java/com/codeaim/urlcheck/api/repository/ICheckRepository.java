package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.model.Check;

import java.util.List;

public interface ICheckRepository
{
    List<Check> getChecks();

    List<Check> getChecks(String username);

    Check createCheck(Check check);
}
