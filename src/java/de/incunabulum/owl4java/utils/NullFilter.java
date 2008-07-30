package de.incunabulum.owl4java.utils;

import com.hp.hpl.jena.util.iterator.Filter;

public class NullFilter extends Filter {

	@Override
	public boolean accept(Object obj) {
		return (obj == null);
	}

}
