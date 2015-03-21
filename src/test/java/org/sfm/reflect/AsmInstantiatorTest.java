package org.sfm.reflect;

import org.junit.Test;
import org.sfm.beans.DbObject;
import org.sfm.csv.CellValueReader;
import org.sfm.csv.impl.*;
import org.sfm.csv.impl.cellreader.DelayedCellSetterImpl;
import org.sfm.reflect.asm.AsmFactory;

import java.sql.ResultSet;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

public class AsmInstantiatorTest {

	AsmFactory factory = new AsmFactory(Thread.currentThread().getContextClassLoader());

	@Test
	public void testInstantiate() throws Exception {
		
		Instantiator<ResultSet, DbObject> instantiator = factory.createEmptyArgsInstantiator(ResultSet.class, DbObject.class);
		
		assertNotNull(instantiator.newInstance(null));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInstantiateStringWithCharArray() throws Exception {
		HashMap<ConstructorParameter, Getter<CsvCellHandlerImpl<String>, ?>> injections = new HashMap<ConstructorParameter, Getter<CsvCellHandlerImpl<String>, ?>>();

		ConstructorParameter parameter = new ConstructorParameter("arg0", char[].class);
		DelayedGetter delayedGetter = new DelayedGetter(0);
		injections.put(parameter, delayedGetter);

		Instantiator<CsvCellHandlerImpl<String>, String> instantiator =
				factory.createInstantiator(CsvCellHandlerImpl.class,
						new ConstructorDefinition<String>(String.class.getConstructor(char[].class), parameter),
						injections);
		DelayedCellSetterImpl delayedCellSetter = new DelayedCellSetterImpl(null, new CellValueReader() {
			@Override
			public char[] read(char[] chars, int offset, int length, ParsingContext parsingContext) {
				return new char[]{'h'};
			}
		});
		delayedCellSetter.set(null, 0, 0, null);

        CsvCellHandlerImpl targetSettersImpl = new CsvCellHandlerImpl(instantiator, new DelayedCellSetter[]{delayedCellSetter}, new CellSetter[]{}, null, null, null);

		assertNotNull(instantiator.newInstance(targetSettersImpl));
	}
}
