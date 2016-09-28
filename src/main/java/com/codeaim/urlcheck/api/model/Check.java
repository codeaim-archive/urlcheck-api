package com.codeaim.urlcheck.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Check
{
    private long id;
    private String name;
    private String url;
    private Status status;
    private Long latestResultId;
    private boolean confirming;
}
