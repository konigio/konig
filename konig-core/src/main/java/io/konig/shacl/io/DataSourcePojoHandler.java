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


import org.openrdf.model.Resource;

import io.konig.core.KonigException;
import io.konig.core.pojo.impl.BasicPojoHandler;
import io.konig.core.pojo.impl.PojoInfo;
import io.konig.datasource.DataSource;
import io.konig.datasource.DataSourceManager;

public class DataSourcePojoHandler extends BasicPojoHandler {

	public DataSourcePojoHandler(Class<?> type) {
		super(type);
	}
	
	protected Object newInstance(PojoInfo pojoInfo) throws KonigException {

		Resource id = pojoInfo.getVertex().getId();
		DataSource ds = DataSourceManager.getInstance().findDataSourceById(id);
		if (ds != null) {
			return ds;
		}
		
		return super.newInstance(pojoInfo);
	}

}
