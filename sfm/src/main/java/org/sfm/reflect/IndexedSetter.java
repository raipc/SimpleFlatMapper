package org.sfm.reflect;


public interface IndexedSetter<T, P> {

	void set(T target, P value, int index) throws Exception;
}
