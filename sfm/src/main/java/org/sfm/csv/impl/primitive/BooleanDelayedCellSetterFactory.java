package org.sfm.csv.impl.primitive;

import org.sfm.csv.mapper.BreakDetector;
import org.sfm.csv.mapper.CsvMapperCellConsumer;
import org.sfm.csv.mapper.DelayedCellSetter;
import org.sfm.csv.mapper.DelayedCellSetterFactory;
import org.sfm.csv.impl.cellreader.BooleanCellValueReader;
import org.sfm.reflect.primitive.BooleanSetter;

public class BooleanDelayedCellSetterFactory<T> implements DelayedCellSetterFactory<T, Boolean> {

	private final BooleanSetter<T> setter;
	private final BooleanCellValueReader reader;

	public BooleanDelayedCellSetterFactory(BooleanSetter<T> setter, BooleanCellValueReader reader) {
		this.setter = setter;
		this.reader = reader;
	}

	@Override
	public DelayedCellSetter<T, Boolean> newCellSetter(BreakDetector breakDetector, CsvMapperCellConsumer<?>[]  cellHandlers) {
		return new BooleanDelayedCellSetter<T>(setter, reader);
	}

    @Override
    public boolean hasSetter() {
        return setter != null;
    }

    @Override
    public String toString() {
        return "BooleanDelayedCellSetterFactory{" +
                "setter=" + setter +
                ", reader=" + reader +
                '}';
    }
}
