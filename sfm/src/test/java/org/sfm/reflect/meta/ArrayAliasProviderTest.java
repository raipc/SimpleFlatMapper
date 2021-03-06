package org.sfm.reflect.meta;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ArrayAliasProviderTest {

    @Test
    public void testProvider() {
        final Table table = new Table(null, null, null);
        ArrayAliasProvider p = new ArrayAliasProvider(new DefaultAliasProvider(), new AliasProvider() {
            @Override
            public String getAliasForMethod(Method method) {
                return "getAliasForMethod";
            }

            @Override
            public String getAliasForField(Field field) {
                return "getAliasForField";
            }

            @Override
            public Table getTable(Class<?> target) {
                return table;
            }
        });

        assertEquals("getAliasForField", p.getAliasForField(null));
        assertEquals("getAliasForMethod", p.getAliasForMethod(null));
        assertSame(table, p.getTable(null));
    }

}