package io.konig.core.showl;

import java.util.List;

public class ShowlArrayExpression extends ShowlListExpression {

	public ShowlArrayExpression() {
	}
	
	public ShowlArrayExpression(List<ShowlExpression> memberList) {
		super(memberList);
	}
	
	@Override
	public ShowlArrayExpression transform() {
		return new ShowlArrayExpression(ShowlUtil.transform(memberList));
	}
	
	

}
