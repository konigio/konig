package io.konig.spreadsheet;

import java.util.ArrayList;
import java.util.List;

public class ListFunctionVisitor implements FunctionVisitor {
	
	private List<Function> list = new ArrayList<>();

	@Override
	public void visit(Function function) {
		list.add(function);
	}

	public List<Function> getList() {
		return list;
	}
	
	

}
