package org.sfm.csv.impl.primitive;

import org.sfm.csv.mapper.CellSetter;
import org.sfm.csv.ParsingContext;
import org.sfm.csv.impl.cellreader.BooleanCellValueReader;
import org.sfm.reflect.primitive.BooleanSetter;

public class BooleanCellSetter<T> implements CellSetter<T> {

	private final BooleanSetter<T> setter;
	private final BooleanCellValueReader reader;
	
	public BooleanCellSetter(BooleanSetter<T> setter, BooleanCellValueReader reader) {
		this.setter = setter;
		this.reader = reader;
	}
	
	@Override
	public void set(T target, char[] chars, int offset, int length, ParsingContext parsingContext)
			throws Exception {
        if (target == null) return;
        setter.setBoolean(target, reader.readBoolean(chars, offset, length, parsingContext));
	}

    @Override
    public String toString() {
        return "BooleanCellSetter{" +
                "setter=" + setter +
                ", reader=" + reader +
                '}';
    }
}
