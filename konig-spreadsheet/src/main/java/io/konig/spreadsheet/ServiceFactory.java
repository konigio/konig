package io.konig.spreadsheet;

public interface ServiceFactory<T> {

	T createInstance();
}
