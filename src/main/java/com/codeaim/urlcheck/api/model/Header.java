package com.codeaim.urlcheck.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Header
{
    private String name;
    private String value;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Header header = (Header) o;

        return name.equals(header.name);
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
