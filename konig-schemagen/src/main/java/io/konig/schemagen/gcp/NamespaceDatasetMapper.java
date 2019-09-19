package io.konig.schemagen.gcp;

import org.openrdf.model.Literal;

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



import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.vocab.GCP;

/**
 * A DatasetMapper that uses the preferred prefix for the namespace
 * of the target OWL class as the ID for the dataset.
 * @author Greg McFall
 *
 */
public class NamespaceDatasetMapper implements DatasetMapper {

	private NamespaceManager nsManager;
	
	

	public NamespaceDatasetMapper(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}



	@Override
	public String datasetForClass(Vertex owlClass) {
		
		Value preferred = owlClass.getValue(GCP.preferredGcpDatasetId);
		if (preferred instanceof Literal) {
			String property = getPropertyFromExpression(preferred.stringValue());
			if(property != null && !property.equals("classNamespacePrefix")) {
				return preferred.stringValue();
			}
		}
		
		Resource id = owlClass.getId();
		if (id instanceof URI) {
			URI uri = (URI) id;
			String name = uri.getNamespace();
			Namespace ns = nsManager.findByName(name);
			if (ns != null) {
				return ns.getPrefix();
			}
		}
		throw new KonigException("Namespace not found for class: " + id);
	}


	private String getPropertyFromExpression( String expression )
    {
        if ( expression != null && expression.startsWith( "${" ) && expression.endsWith( "}" )
            && !expression.substring( 2 ).contains( "${" ) )
        {
            // expression="${xxx}" -> property="xxx"
            return expression.substring( 2, expression.length() - 1 );
        }
        // no property can be extracted
        return null;
    }
	
	@Override
	public String getId(Vertex owlClass) {
		return datasetForClass(owlClass);
	}

}
