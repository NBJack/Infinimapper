package org.rpl.infinimapper.data.management;

/**
 * A class that can be incrementally changed using type <Detla>.
 * 
 * @author Ryan
 * 
 * @param <Delta> The class type that can be applied.
 */
public interface Incrementable<T, Delta> {

	/**
	 * Applies a delta to the existing state and create a new object reflecting
	 * the change.
	 * 
	 * @param delta The difference to apply. Cannot be null.
	 */
	public T applyDelta(Delta delta);

}
