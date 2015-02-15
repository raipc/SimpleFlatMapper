package org.sfm.jdbc.impl.getter;

import org.sfm.reflect.Getter;

import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class RefResultSetGetter implements Getter<ResultSet, Ref> {
	private final int column;
	
	public RefResultSetGetter(final int column) {
		this.column = column;
	}

	public Ref get(final ResultSet target) throws SQLException {
		return target.getRef(column);
	}
}