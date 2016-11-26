package com.codeaim.urlcheck.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true)
public class Event
{
    private long id;
    private long checkId;
    private Instant start;
    private Instant end;
    private Status startStatus;
    private Status endStatus;
    private Long duration;
}
