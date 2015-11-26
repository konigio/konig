package io.konig.shacl.impl;

import java.util.ArrayList;

import io.konig.core.KonigException;

public class EmptyList<T> extends ArrayList<T> {
	private static final long serialVersionUID = 1L;
	
	public boolean add(T element) {
		throw new KonigException("The add operation is not supported on this read-only list");
	}

}
