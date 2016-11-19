package com.codeaim.urlcheck.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EmailVerification
{
    private String email;
    private String username;
    private String emailVerificationToken;
}
