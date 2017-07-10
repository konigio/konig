package io.konig.schemagen.java;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JVar;

import io.konig.core.NamespaceManager;

public class NamespacesBuilder {
	
	private JavaNamer namer;
	
	
	public NamespacesBuilder(JavaNamer namer) {
		this.namer = namer;
	}


	public void generateNamespaces(JCodeModel model, NamespaceManager nsManager)  {
		
		 try {
			 
			JClass hashMapClass = model.ref(HashMap.class).narrow(String.class, Namespace.class);
			JClass mapClass = model.ref(Map.class).narrow(String.class, Namespace.class);
			JDefinedClass dc = model._class(namer.namespacesClass());
			JVar mapField = dc.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, mapClass, "map", JExpr._new(hashMapClass));
			generateStaticInit(model, dc, nsManager);
			generateAddMethod(model, dc, mapField);
			generateCurieMethod(model, dc, mapField);
			
		} catch (JClassAlreadyExistsException e) {
		}
		
		
	}


	private void generateStaticInit(JCodeModel model, JDefinedClass dc, NamespaceManager nsManager) {
		
		JBlock block = dc.init();
		
		List<Namespace> list = new ArrayList<>(nsManager.listNamespaces());
		Collections.sort(list, new Comparator<Namespace>() {

			@Override
			public int compare(Namespace a, Namespace b) {
				return a.getPrefix().compareTo(b.getPrefix());
			}
		});
		
		for (Namespace ns : list) {
			
			JExpression prefixValue = JExpr.lit(ns.getPrefix());
			JExpression nameValue = JExpr.lit(ns.getName());
			
			block.add(dc.staticInvoke("add").arg(prefixValue).arg(nameValue));
		}
		
	}


	private void generateCurieMethod(JCodeModel model, JDefinedClass dc, JVar mapField) {

		JClass stringBuilderClass = model.ref(StringBuilder.class);
		JClass namespaceClass = model.ref(Namespace.class);
		
		JMethod method = dc.method(JMod.PUBLIC | JMod.STATIC, String.class, "curie");
		JVar uriVar = method.param(URI.class, "uri");
		
		JVar nsVar = method.body().decl(namespaceClass, "ns", mapField.invoke("get").arg(uriVar.invoke("getNamespace")));
		
		method.body()._if(JOp.eq(nsVar, JExpr._null()))._then()._return(uriVar.invoke("stringValue"));
		
		JVar builderVar = method.body().decl(stringBuilderClass, "builder", JExpr._new(stringBuilderClass));
		
		method.body().add(builderVar.invoke("add").arg(nsVar.invoke("getPrefix")));
		method.body().add(builderVar.invoke("add").arg(JExpr.lit(':')));
		method.body().add(builderVar.invoke("add").arg(uriVar.invoke("getLocalName")));
		
		method.body()._return(builderVar.invoke("toString"));
		
	}


	private void generateAddMethod(JCodeModel model, JDefinedClass dc, JVar mapField) {
		JClass namespaceClass = model.ref(Namespace.class);
		JMethod method = dc.method(JMod.PRIVATE | JMod.STATIC, void.class, "add");
		JVar prefixVar = method.param(String.class, "prefix");
		JVar nameVar = method.param(String.class, "name");
		JInvocation nsInvoke = JExpr._new(namespaceClass);
		nsInvoke.arg(prefixVar).arg(nameVar);
		
		JVar nsVar = method.body().decl(namespaceClass, "ns", nsInvoke);
		method.body().invoke(mapField, "put").arg(prefixVar).arg(nsVar);
		method.body().invoke(mapField, "put").arg(nameVar).arg(nsVar);
		
	}
	

}
