package org.sfm.map;

import org.sfm.reflect.Getter;
import org.sfm.reflect.Setter;

public final class FieldMapperImpl<S, T, P> implements FieldMapper<S, T> {
	
	private final Getter<S, ? extends P> getter;
	private final Setter<T, P> setter;
	
	public FieldMapperImpl(final Getter<S, ? extends P> getter, final Setter<T, P> setter) {
		this.getter = getter;
		this.setter = setter;
	}
	
	@Override
	public void map(final S source, final T target) throws Exception {
		final P value = getter.get(source);
		setter.set(target, value);
	}
}
