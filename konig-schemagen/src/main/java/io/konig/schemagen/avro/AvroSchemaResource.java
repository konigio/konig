package io.konig.schemagen.avro;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.io.impl.JsonUtil;

public class AvroSchemaResource {
	private String text;
	private String schemaName;
	private int usageCount;
	
	public AvroSchemaResource(String text, String schemaName, int usageCount) {
		this.text = text;
		this.schemaName = schemaName;
		this.usageCount = usageCount;
	}

	public String getText() {
		return text;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public int getUsageCount() {
		return usageCount;
	}
	
	public ObjectNode toObjectNode() {
		return JsonUtil.parse(text);
	}
}
