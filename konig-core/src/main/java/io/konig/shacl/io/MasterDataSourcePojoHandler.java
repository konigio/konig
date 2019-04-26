package io.konig.shacl.io;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.HashMap;
import java.util.Map;

import io.konig.core.KonigException;
import io.konig.core.pojo.impl.PojoHandler;
import io.konig.core.pojo.impl.PojoInfo;
import io.konig.core.pojo.impl.PojoUtil;

public class MasterDataSourcePojoHandler implements PojoHandler {

	private Map<Class<?>, PojoHandler> handlerMap = new HashMap<>();
	
	@Override
	public void buildPojo(PojoInfo pojoInfo) throws KonigException {
		
		Class<?> javaType = PojoUtil.selectType(pojoInfo);
		
		PojoHandler handler = handlerMap.get(javaType);
		if (handler == null) {
			handler = new DataSourcePojoHandler(javaType);
			handlerMap.put(javaType, handler);
		}
		
		handler.buildPojo(pojoInfo);
	}
	
}
