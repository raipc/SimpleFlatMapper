package org.sfm.reflect.primitive;

import org.sfm.reflect.Getter;

public class BoxedCharacterGetter<T> implements CharacterGetter<T>, Getter<T, Character> {


    private final Getter<? super T, Character> delegate;

    public BoxedCharacterGetter(Getter<? super T, Character> delegate) {
        this.delegate = delegate;
    }

    @Override
    public char getCharacter(T target) throws Exception {
        final Character value = get(target);
        if (value != null) {
            return value.charValue();
        }
        return 0;
    }

    @Override
    public Character get(T target) throws Exception {
        return delegate.get(target);
    }
}
