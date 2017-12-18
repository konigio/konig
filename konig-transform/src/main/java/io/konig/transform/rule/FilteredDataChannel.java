package io.konig.transform.rule;

import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.Shape;

public class FilteredDataChannel extends DataChannel {

	private QuantifiedExpression formula;
	private DataChannel rawDataChannel;
	
	public FilteredDataChannel(Shape shape, DataChannel rawDataChannel) {
		super(shape, rawDataChannel.getDatasource());
	}

	public QuantifiedExpression getFormula() {
		return formula;
	}

	public void setFormula(QuantifiedExpression formula) {
		this.formula = formula;
	}

	public DataChannel getRawDataChannel() {
		return rawDataChannel;
	}

	public void setRawDataChannel(DataChannel rawDataChannel) {
		this.rawDataChannel = rawDataChannel;
	}

	

}
