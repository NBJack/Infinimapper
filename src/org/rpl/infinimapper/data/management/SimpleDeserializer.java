package org.rpl.infinimapper.data.management;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Provides simple deserialization of an object. This is a basic operation set,
 * as no nested objects are currently permitted and inheritance isn't supported.
 * Field names must match one to one with column names, and only a subset of
 * object types are supported.
 * 
 * @author rplayfield
 * 
 */
public abstract class SimpleDeserializer {

	private enum FieldType {

		Integer(Integer.class, "int"), Float(Float.class, "float"), String(String.class, null), Double(Double.class,
				"double"), Boolean(Boolean.class, "boolean");

		Class<?> type;
		String primitiveName;

		FieldType(Class<?> c, String primitiveName) {
			this.type = c;
			this.primitiveName = primitiveName;
		}

		public Class<?> getType() {
			return type;
		}

		/**
		 * Matches the primitive name, if it exists.
		 * 
		 * @param name
		 * @return
		 */
		public boolean matchesPrimitive(String name) {
			return this.primitiveName != null && name != null && name.equals(primitiveName);
		}

		/**
		 * Find the type by enum. TODO: Make this faster via a map.
		 * 
		 * @param type
		 * @return
		 */
		public static FieldType findType(Class<?> type) {
			for (FieldType f : FieldType.values()) {
				if (f.getType().equals(type) || f.matchesPrimitive(type.getName()))
					return f;
			}

			System.err.println("Unable to find type " + type.getName());

			throw new IllegalArgumentException();
		}
	}

	/**
	 * Sets a single field of the target object based on the type offered by
	 * field from the current record in the result set source.
	 * 
	 * @param <T>
	 * @param target The target object. Presumed not to be null.
	 * @param field The field of the target to set. Presumed not to be null.
	 * @param source The result set to draw the data from. Presumed not to be
	 *            null and presumed to be at an active, valid record.
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private static <T> void setAppropriateFieldByName(T target, Field field, ResultSet source, String columnName)
			throws IllegalArgumentException, IllegalAccessException, SQLException {
		FieldType type = FieldType.findType(field.getType());

		switch (type) {
		case Integer:
			field.setInt(target, source.getInt(columnName));
			break;
		case Double:
			field.setDouble(target, source.getDouble(columnName));
			break;
		case String:
			field.set(target, source.getString(columnName));
			break;
		case Float:
			field.setFloat(target, source.getFloat(columnName));
			break;
		case Boolean:
			field.setBoolean(target, source.getBoolean(columnName));
			break;
		default:
			System.err.println("I don't know how to deserialize '" + type + "'");
		}
	}

	/**
	 * Deserialize a set of objects from the ResultSet.
	 * 
	 * @param <T>
	 * @param set
	 * @param classDef The class type to deserialize.
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 */
	public static <T> List<T> deserializeFromSet(ResultSet set, Class<T> classDef) throws InstantiationException,
			IllegalAccessException, SQLException {


		// Map all class fields. While we're at it, make them accessible.
		Map<String, Field> fieldMap = new LinkedHashMap<String, Field>();
		for (Field f : classDef.getDeclaredFields()) {
			fieldMap.put(f.getName(), f);
			if (!f.isAccessible()) {
				f.setAccessible(true);
			}
		}

		// Iterate through the result set, instantiate each object, and fill it
		// out.
		List<T> results = new LinkedList<T>();
		while (set.next()) {
			T target = classDef.newInstance();
			for (Entry<String, Field> entry : fieldMap.entrySet()) {
                // Skip static fields
                if ( !Modifier.isStatic(entry.getValue().getModifiers()) ) {
				    setAppropriateFieldByName(target, entry.getValue(), set, entry.getKey());
                }
			}
			results.add(target);
		}

		return results;
	}
}
