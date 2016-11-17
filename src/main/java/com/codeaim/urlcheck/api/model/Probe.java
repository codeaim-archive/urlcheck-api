package com.codeaim.urlcheck.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Probe
{
    private String name;
    private boolean clustered;
    private long candidateLimit;
    private String username;
}
