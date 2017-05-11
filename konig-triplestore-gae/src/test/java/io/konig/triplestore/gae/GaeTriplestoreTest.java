package io.konig.triplestore.gae;

import io.konig.triplestore.core.Triplestore;

public class GaeTriplestoreTest extends AbstractGaeTriplestoreTest {

	@Override
	Triplestore createTriplestore() {
		return new GaeTriplestore();
	}

}
