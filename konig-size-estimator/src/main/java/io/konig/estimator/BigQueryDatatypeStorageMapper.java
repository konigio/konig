package io.konig.estimator;

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
		}
		return value;
	}
}
