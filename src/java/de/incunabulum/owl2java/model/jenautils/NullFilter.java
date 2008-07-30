package de.incunabulum.owl2java.model.jenautils;

import com.hp.hpl.jena.util.iterator.Filter;

public class NullFilter extends Filter {

	@Override
	public boolean accept(Object obj) {
		return (obj == null);
	}

}
