package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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