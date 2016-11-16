package com.codeaim.urlcheck.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private Set<Header> headers;
    private Instant disabled;
    private boolean internal;
}
