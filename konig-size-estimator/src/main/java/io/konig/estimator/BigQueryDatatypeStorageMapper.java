package io.konig.estimator;

/*
 * #%L
 * Konig Size Estimator
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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

import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import io.konig.schemagen.gcp.BigQueryDatatype;
import io.konig.schemagen.gcp.BigQueryDatatypeMapper;
import io.konig.shacl.PropertyConstraint;

public class BigQueryDatatypeStorageMapper {
	private BigQueryDatatypeMapper mapper = new BigQueryDatatypeMapper();

	public int getDataSize(PropertyConstraint propertyConstraint, Object data) {
		int value = 0;

		BigQueryDatatype type = mapper.type(propertyConstraint);
		if ((type == BigQueryDatatype.INT64) || (type == BigQueryDatatype.FLOAT64)
				|| (type == BigQueryDatatype.TIMESTAMP)) {
			value = 8;
		} else if (type == BigQueryDatatype.BOOLEAN) {
			value = 4;
		} else if (type == BigQueryDatatype.STRING) {
			String stringData = (String) data;
			value = 2 * stringData.length();
		} else if (type == BigQueryDatatype.RECORD) {
			value = -1;		
		}
		return value;
	}
}
