package io.konig.core.showl;

import java.util.List;

public class ShowlAlternativePathsExpression extends ShowlListExpression {

	public ShowlAlternativePathsExpression() {
	}

	public ShowlAlternativePathsExpression(List<ShowlExpression> memberList) {
		super(memberList);
	}


	@Override
	public ShowlAlternativePathsExpression transform() {
		return new ShowlAlternativePathsExpression(ShowlUtil.transform(memberList));
	}
	

}
