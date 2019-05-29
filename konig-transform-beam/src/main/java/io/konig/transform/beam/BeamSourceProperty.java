package io.konig.transform.beam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.URI;

import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;

public class BeamSourceProperty implements Comparable<BeamSourceProperty> {

	private BeamChannel beamChannel;
	private ShowlPropertyShape propertyShape;
	private JVar var;

	public BeamSourceProperty(BeamChannel beamChannel, ShowlPropertyShape propertyShape) {
		this.beamChannel = beamChannel;
		this.propertyShape = propertyShape;
	}

	public BeamChannel getBeamChannel() {
		return beamChannel;
	}
	
	public URI getPredicate() {
		return propertyShape.getPredicate();
	}

	public JVar getVar() {
		return var;
	}

	public void setVar(JVar var) {
		this.var = var;
	}

	public ShowlPropertyShape getPropertyShape() {
		return propertyShape;
	}

	@Override
	public int compareTo(BeamSourceProperty o) {
		return propertyShape.getPredicate().getLocalName().compareTo(o.getPropertyShape().getPredicate().getLocalName());
	}

	public String canonicalPath() {
		return propertyShape.getPath();
	}
	
}
