package com.codeaim.urlcheck.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Check
{
    private long id;
    @JsonIgnore
    private long userId;
    @NotNull
    private String name;
    @NotNull
    private String url;
    private Status status;
    private Long latestResultId;
    private boolean confirming;
    @NotNull
    private Integer interval;
}
