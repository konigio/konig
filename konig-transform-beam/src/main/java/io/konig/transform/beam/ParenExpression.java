package io.konig.transform.beam;

import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.IJFormatter;

@SuppressWarnings("serial")
public class ParenExpression implements IJExpression {

  private IJExpression expression;

  public ParenExpression(IJExpression expression) {
      this.expression = expression;
  }

  @Override
  public void generate(IJFormatter formatter) {
      formatter.print('(');
      expression.generate(formatter);
      formatter.print(')');
  }
}