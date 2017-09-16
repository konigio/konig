package io.konig.core.pojo;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class ReflectionTest {

	public static void main(String[] args) throws Exception {
		
		Method[] methodList = Organization.class.getMethods();
		
		for (Method m : methodList) {
			if (m.getName().equals("setPersonList")) {
				
				

				Type genericParamType = m.getGenericParameterTypes()[0];
				if (genericParamType instanceof ParameterizedType) {
					ParameterizedType pType = (ParameterizedType) genericParamType;
					Type[] argTypes = pType.getActualTypeArguments();
					if (argTypes.length==1) {
						Class<?> argType = (Class<?>) argTypes[0];
						
						System.out.println(argType);
					}
				}
				
			}
		}

	}
	
	static class Organization {
		public void setPersonList(List<TestPerson> list) {
	
		}
	}

}
