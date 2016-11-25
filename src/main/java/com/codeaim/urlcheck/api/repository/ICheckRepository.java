package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.model.Check;

import java.util.List;
import java.util.Optional;

public interface ICheckRepository
{
    List<Check> getChecksByUsername();

    List<Check> getChecksByUsername(String username);

    List<Check> getChecksWithEventsByUsername(String username);

    Optional<Check> getCheckById(Long id);

    Check createCheck(Check check);

    void deleteCheck(long id);

    Check updateCheck(Check check);

    boolean checkExists(long id);
}
