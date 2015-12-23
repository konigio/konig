package io.konig.services;

import io.konig.core.KonigTest;
import io.konig.services.impl.BaseKonigConfig;

public class KonigServiceTest extends KonigTest {
	
	protected KonigConfig config;
	
	protected KonigConfig config() {
		if (config == null) {
			config = new BaseKonigConfig();
		}
		return config;
	}

}
