package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO SQL Runtime
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


import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.cloud.bigquery.FieldValue;

public class BigQueryUtil {

	public static Object dataValue(FieldValue fieldValue, String dataType) {
		switch (dataType) {
		case "http://www.w3.org/2001/XMLSchema#string":
			return fieldValue.getStringValue();
		case "http://www.w3.org/2001/XMLSchema#dateTime":
			return new DateTime(fieldValue.getTimestampValue() / 1000).toDateTime(DateTimeZone.UTC);
		case "http://www.w3.org/2001/XMLSchema#date":
			return new DateTime(fieldValue.getStringValue()).toDate();
		case "http://www.w3.org/2001/XMLSchema#time":
			return new DateTime(fieldValue.getStringValue());
		case "http://www.w3.org/2001/XMLSchema#int":
			return new Integer((int) fieldValue.getLongValue());
		case "http://www.w3.org/2001/XMLSchema#long":
			return fieldValue.getLongValue();
		case "http://www.w3.org/2001/XMLSchema#float":
			return fieldValue.getDoubleValue();
		case "http://www.w3.org/2001/XMLSchema#double":
			return fieldValue.getDoubleValue();
		case "http://www.w3.org/2001/XMLSchema#boolean":
			return fieldValue.getBooleanValue();
		default:
			return fieldValue.getValue();
		}
	}
}
