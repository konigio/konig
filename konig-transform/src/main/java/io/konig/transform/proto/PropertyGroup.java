package io.konig.transform.proto;

import java.io.StringWriter;

/*
 * #%L
 * Konig Transform
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
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.io.PrettyPrintable;

public class PropertyGroup extends ArrayList<PropertyModel> implements PrettyPrintable {
	private static final long serialVersionUID = 1L;
	
	private static int counter=0;
	private PropertyModel targetProperty;
	private PropertyModel sourceProperty;
	
	private ClassModel parentClassModel;
	private ClassModel valueClassModel;
	private int id = counter++;
	
	public PropertyGroup() {
		
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	

	public PropertyModel getTargetProperty() {
		return targetProperty;
	}

	public void setTargetProperty(PropertyModel targetProperty) {
		this.targetProperty = targetProperty;
	}

	public ClassModel getValueClassModel() {
		return valueClassModel;
	}

	public void setValueClassModel(ClassModel valueClassModel) {
		this.valueClassModel = valueClassModel;
	}

	public ClassModel produceValueClassModel(URI owlClass) {
		if (valueClassModel == null) {
			valueClassModel = new ClassModel(owlClass);
		} else if (valueClassModel.getOwlClass()==null) {
			valueClassModel.setOwlClass(owlClass);
		}
		return valueClassModel;
	}

	public PropertyModel getSourceProperty() {
		return sourceProperty;
	}

	public void setSourceProperty(PropertyModel sourceProperty) {
		this.sourceProperty = sourceProperty;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("targetProperty", targetProperty);
		out.field("sourceProperty", sourceProperty);
		
		int count = 
			(targetProperty==null ? 0 : 1) + 
			(sourceProperty==null ? 0 : 1);
		
		if (size()>count) {
			out.beginArray("contains");
			for (PropertyModel p : this) {
				if (p!=targetProperty && p!=sourceProperty) {
					out.print(p);
				}
			}
			out.endArray("contains");
		}
		out.endObject();
		
		out.field("valueClassModel", valueClassModel);
		
	}
	

	public ClassModel getParentClassModel() {
		return parentClassModel;
	}

	public void setParentClassModel(ClassModel parentClassModel) {
		this.parentClassModel = parentClassModel;
	}

	public String toString() {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter out = new PrettyPrintWriter(buffer);
		print(out);
		out.close();
		return buffer.toString();
	}

	

}
