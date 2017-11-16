package io.konig.transform.rule;

import io.konig.transform.proto.PropertyModel;

abstract public class AbstractIdRule implements IdRule {

	private PropertyModel sourcePropertyModel;
	

	@Override
	public PropertyModel getSourcePropertyModel() {
		return sourcePropertyModel;
	}


	public void setSourcePropertyModel(PropertyModel sourcePropertyModel) {
		this.sourcePropertyModel = sourcePropertyModel;
	}
	
	

}
