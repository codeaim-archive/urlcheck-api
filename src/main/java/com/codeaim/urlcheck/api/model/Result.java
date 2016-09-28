package com.codeaim.urlcheck.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result
{
    private long id;
    private long checkId;
    private Long previousResultId;
    private Status status;
    private String probe;
    private int statusCode;
    private int responseTime;
    private boolean changed;
    private boolean confirmation;
    private Instant created;
}
