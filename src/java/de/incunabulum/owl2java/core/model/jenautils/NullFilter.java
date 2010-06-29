package de.incunabulum.owl2java.core.model.jenautils;

import com.hp.hpl.jena.util.iterator.Filter;

public class NullFilter<T> extends Filter<T> {

	@Override
	public boolean accept(T obj) {
		return (obj == null);
	}

}
