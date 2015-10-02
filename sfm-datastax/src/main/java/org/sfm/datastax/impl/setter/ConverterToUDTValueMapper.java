package org.sfm.datastax.impl.setter;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import org.sfm.map.Mapper;
import org.sfm.utils.conv.Converter;

public class ConverterToUDTValueMapper<I> implements Converter<I, UDTValue> {

    private final Mapper<I, UDTValue> mapper;
    private final UserType userType;

    public ConverterToUDTValueMapper(Mapper<I, UDTValue> mapper, UserType userType) {
        this.mapper = mapper;
        this.userType = userType;
    }

    @Override
    public UDTValue convert(I in) throws Exception {
        UDTValue udtValue = userType.newValue();
        mapper.mapTo(in, udtValue, null);
        return udtValue;
    }
}
