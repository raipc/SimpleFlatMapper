package org.sfm.csv.impl.cellreader.joda;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.sfm.csv.CellValueReader;
import org.sfm.csv.ParsingContext;
import org.sfm.csv.impl.cellreader.StringCellValueReader;

public class JodaLocalTimeCellValueReader implements CellValueReader<LocalTime> {
    private final DateTimeFormatter fmt;

    public JodaLocalTimeCellValueReader(DateTimeFormatter fmt) {
        this.fmt = fmt;
    }

    @Override
    public LocalTime read(char[] chars, int offset, int length, ParsingContext parsingContext) {
        if (length == 0) return null;
        return fmt .parseLocalTime(StringCellValueReader.readString(chars, offset, length));
    }
}
