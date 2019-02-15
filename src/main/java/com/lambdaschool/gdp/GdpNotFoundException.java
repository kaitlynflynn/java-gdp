package com.lambdaschool.gdp;

public class GdpNotFoundException extends RuntimeException
{
    public GdpNotFoundException (Long id)
    {
        super("Could not find GDP");
    }
}
