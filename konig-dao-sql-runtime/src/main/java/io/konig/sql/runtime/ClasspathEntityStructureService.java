package io.konig.sql.runtime;

import java.io.BufferedReader;

/*
 * #%L
 * Konig DAO SQL Runtime
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


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import io.konig.dao.core.DaoConstants;
import io.konig.dao.core.DaoException;
import io.konig.yaml.YamlParseException;
import io.konig.yaml.YamlReader;

/**
 * An EntityStructureService that loads EntityStructure instances via the ClassLoader.
 * <p>
 * This service loads a list of namespaces from a Turtle file within the classpath.
 * By default, this file should be located at "ClasspathObjectStructureService/namespaces.ttl".
 * You can specify a different location via the {@link #setNamespacesResource(String)} method.
 * </p>
 * <p>
 * This file defines a prefix for each namespace that contains SHACL shape definitions.
 * For instance, it might look something like this:
 * </p>
 * <pre>
 * {@literal @}prefix shape: <http://example.com/shapes/> .
 * </pre>
 * <p>
 * The same folder that contains this file should contain a sub-folder for each namespace.
 * The name of the sub-folder is given by the namespace prefix.  
 * Each sub-folder contains a collection of YAML files with names of the form:
 * <pre>
 * 	{shapeLocalName}.yaml
 * </pre>
 * </p>
 * <p>
 * 
 * </p>
 * @author Greg McFall
 *
 */
public class ClasspathEntityStructureService implements EntityStructureService {
	
	private static final String MEDIA_TYPE_MAP_RESOURCE = "ClasspathEntityStructureService/" + DaoConstants.MEDIA_TYPE_MAP_FILE_NAME;
	private static final String OWL_CLASS_MAP_RESOURCE = "ClasspathEntityStructureService/" + DaoConstants.OWL_CLASS_MAP_FILE_NAME;
	
	private String namespacesResource = "ClasspathEntityStructureService/namespaces.ttl";
	private String basePath;
	private Map<String,String> namespaceMap;
	private Map<String,String> owlClassMap;
	private Map<String,EntityStructure> tableStructureByShapeId;
	
	/**
	 * Map where the key is a media type base name, and the value is the corresponding IRI for the associated Shape.
	 */
	private Map<String,String> mediaTypeMap;
	
	private static ClasspathEntityStructureService INSTANCE;
	
	public static ClasspathEntityStructureService defaultInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ClasspathEntityStructureService();
		}
		return INSTANCE;
	}
	
	private ClasspathEntityStructureService() {
		
	}
	
	
	public String getNamespacesResource() {
		return namespacesResource;
	}

	public void setNamespacesResource(String namespacesResource) {
		this.namespacesResource = namespacesResource;
	}

	@Override
	public EntityStructure structureOfShape(String shapeId) throws DaoException {
		if (namespaceMap == null) {
			init();
		}
		
		EntityStructure result = tableStructureByShapeId.get(shapeId);
		if (result == null) {

			String namespace = namespace(shapeId);
			if (namespace != null) {
				String prefix = namespaceMap.get(namespace);
				if (prefix != null) {
					String localName = shapeId.substring(namespace.length());
					StringBuilder builder = new StringBuilder();
					builder.append(basePath);
					builder.append(prefix);
					builder.append('/');
					builder.append(localName);
					builder.append(".yaml");
					
					String resource = builder.toString();
					InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
					if (input != null) {
						try (YamlReader yaml = new YamlReader(input)) {
							result = yaml.readObject(EntityStructure.class);
							tableStructureByShapeId.put(shapeId, result);
						} catch (IOException | YamlParseException e) {
							throw new DaoException(e);
						}
					}
				}
			}
		}
		
		return result;
	}

	private String namespace(String shapeId) throws DaoException {
		int mark = shapeId.lastIndexOf('#');
		if (mark < 0) {
			int colon = shapeId.lastIndexOf(':');
			int slash = shapeId.lastIndexOf('/');
			
			mark = Math.max(colon, slash);
			if (mark < 0) {
				throw new DaoException("Invalid IRI " + shapeId);
			}
		}
		return shapeId.substring(0, mark+1);
	}

	public void init() throws DaoException {
		if (namespaceMap == null) {
			loadNamespaceMap();
			loadMediaTypeMap();
			loadOwlClassMap();
		}
		
	}
	


	private void loadOwlClassMap() throws DaoException {
		loadMap(owlClassMap = new HashMap<>(), OWL_CLASS_MAP_RESOURCE);
		
	}
	
	private void loadMap(Map<String,String> map, String resourcePath) throws DaoException {
		InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath);
		
		if (input == null) {
			throw new DaoException("Resource not found: " + resourcePath);
		}
		
		try (
			InputStreamReader rawReader = new InputStreamReader(input);
			BufferedReader reader = new BufferedReader(rawReader);
		) {
			String line = new String();
			while ( (line = reader.readLine()) != null) {
				int comma = line.indexOf(',');
				if (comma > 0) {
					String mediaTypeName = line.substring(0, comma).trim();
					String shapeId = line.substring(comma+1).trim();
					map.put(mediaTypeName, shapeId);
				}
			}
			
		} catch (IOException e) {
			throw new DaoException(e);
		}
		
		
	}

	private void loadMediaTypeMap() throws DaoException {
		loadMap(mediaTypeMap = new HashMap<>(), MEDIA_TYPE_MAP_RESOURCE);
	}


	private void loadNamespaceMap() throws DaoException {
		namespaceMap = new HashMap<>();
		tableStructureByShapeId = new HashMap<>();
		int slash = namespacesResource.lastIndexOf('/');
		if (slash < 0) {
			basePath = "";
		} else {
			basePath = namespacesResource.substring(0, slash+1);
		}
	
		InputStream input = getClass().getClassLoader().getResourceAsStream(namespacesResource);
	
		if (input == null) {
			throw new DaoException("Resource not found: " + namespacesResource);
		}
		
		try (
			InputStreamReader reader = new InputStreamReader(input);
			NamespaceReader nsReader = new NamespaceReader(reader);
		) {
			namespaceMap = nsReader.readNamespaces();
			
		} catch (IOException e) {
			throw new DaoException(e);
		}
		
	}


	@Override
	public EntityStructure forMediaType(String mediaTypeBaseName) throws DaoException {
		if(mediaTypeMap == null) {
			init();
		}
		String shapeId = mediaTypeMap.get(mediaTypeBaseName);
		if (shapeId == null) {
			throw new DaoException("EntityStructure not found for media type: " + mediaTypeBaseName);
		}
		return structureOfShape(shapeId);
	}

	@Override
	public EntityStructure defaultForOwlClass(String localName) throws DaoException {
		loadOwlClassMap();		
		String shapeId = owlClassMap.get(localName.toLowerCase());
		if (shapeId == null) {
			throw new DaoException("EntityStructure not found for OWL class: " + localName);
		}
		return structureOfShape(shapeId);
	}

}
