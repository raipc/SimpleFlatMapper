package org.sfm.reflect.asm.sample;

import org.sfm.beans.DbObject;
import org.sfm.csv.CsvColumnKey;
import org.sfm.csv.impl.*;
import org.sfm.map.FieldMapperErrorHandler;
import org.sfm.reflect.Instantiator;

public class AsmCsvCellHandlerFactory extends CsvCellHandlerFactory<DbObject> {
    public AsmCsvCellHandlerFactory(Instantiator<CsvCellHandler<DbObject>, DbObject> instantiator, CsvColumnKey[] keys, ParsingContextFactory parsingContextFactory, FieldMapperErrorHandler<CsvColumnKey> fieldErrorHandler) {
        super(instantiator, keys, parsingContextFactory, fieldErrorHandler);
    }

    @Override
    public CsvCellHandler<DbObject> newInstace(DelayedCellSetter<DbObject, ?>[] delayedSetters, CellSetter<DbObject>[] setters) {
        return new AsmCsvCellHandler(instantiator, delayedSetters, setters, keys, parsingContextFactory.newContext(), fieldErrorHandler);
    }
}
