package org.simpleflatmapper.jdbc.impl.convert.time;

import org.simpleflatmapper.converter.Converter;

import java.sql.Date;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;

public class JavaYearToDateConverter implements Converter<Year, Date> {
    private final ZoneId zoneId;

    public JavaYearToDateConverter(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public Date convert(Year in) throws Exception {
        if (in == null) return null;
        return new Date(in.atMonthDay(MonthDay.of(Month.JANUARY, 1)).atStartOfDay(zoneId).toInstant().toEpochMilli());
    }
}