package com.atlassian.util.benchmark;

import java.text.NumberFormat;

public class Util
{
    private static final NumberFormat format = NumberFormat.getInstance();

    static String format(long number)
    {
        return format.format(number);
    }
}