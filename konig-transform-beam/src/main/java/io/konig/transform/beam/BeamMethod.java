package io.konig.transform.beam;

import java.util.ArrayList;
import java.util.List;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;

public class BeamMethod {
	
	
	private JMethod method;
	private List<BeamParameter> parameters = new ArrayList<>();
	
	public BeamMethod(JMethod method) {
		this.method = method;
	}
		public void addParameter(BeamParameter p) {
		parameters.add(p);
	}

	public JMethod getMethod() {
		return method;
	}

	public List<BeamParameter> getParameters() {
		return parameters;
	}
	
	public BeamParameter addListParam(AbstractJType paramType, String paramName) {
		JVar var = method.param(paramType, paramName);
		BeamParameter param = BeamParameter.ofList(var);
		addParameter(param);
		
		return param;
	}

	public void addErrorBuilderParam(AbstractJClass errorBuilderClass) {
		JVar param = method.param(errorBuilderClass, "errorBuilder");
		addParameter(BeamParameter.ofErrorBuilder(param));
	}

}
