package com.liweiyap.xkcdbrowser.util;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateFormatter
{
    private DateFormatter(){}

    @NotNull
    public static String formatDate(final String day, final String month, final String year)
    {
        if ((day == null) || (month == null) || (year == null))
        {
            return "";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ENGLISH);

        String dateString = String.format(
            "%s/%s/%s",
            String.format(java.util.Locale.ENGLISH,"%02d", Integer.valueOf(day)),
            String.format(java.util.Locale.ENGLISH,"%02d", Integer.valueOf(month)),
            String.format(java.util.Locale.ENGLISH,"%02d", Integer.valueOf(year))
        );

        Date date = null;

        try
        {
            date = simpleDateFormat.parse(dateString);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        if (date == null)
        {
            return "";
        }

        simpleDateFormat.applyPattern("EEE, d MMM yyyy");
        return simpleDateFormat.format(date);
    }
}