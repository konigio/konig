package io.konig.data.app.generator;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.URI;

import io.konig.data.app.common.BasicDataApp;
import io.konig.data.app.common.DataApp;
import io.konig.data.app.common.ExtentContainer;
import io.konig.openapi.model.MediaType;
import io.konig.openapi.model.MediaTypeMap;
import io.konig.openapi.model.OpenAPI;
import io.konig.openapi.model.Operation;
import io.konig.openapi.model.Path;
import io.konig.openapi.model.Response;
import io.konig.openapi.model.ResponseMap;
import io.konig.shacl.MediaTypeManager;
import io.konig.shacl.Shape;

public class DataAppGenerator {
	
	private MediaTypeManager mediaTypeManager;
	private Pattern extentPath = Pattern.compile("/([^/]+)/\\{([^}]+)\\}$");

	public DataAppGenerator(MediaTypeManager mediaTypeManager) {
		this.mediaTypeManager = mediaTypeManager;
	}



	public DataApp toDataApp(OpenAPI openapi) throws DataAppGeneratorException {
		
		BasicDataApp app = new BasicDataApp();
		for (Path path : openapi.getPaths().values()) {
			handlePath(app, path);
		}
		return app;
	}



	private void handlePath(BasicDataApp app, Path path) throws DataAppGeneratorException {
		
		String pathValue = path.stringValue();
		Matcher extentMatcher = extentPath.matcher(pathValue);
		if (extentMatcher.find()) {
			String slug = extentMatcher.group(1);
			Shape defaultShape = defaultShape(path);
			if (defaultShape != null) {
				URI extentClass = defaultShape.getTargetClass();
				ExtentContainer container = new ExtentContainer();
				container.setDefaultShape((URI)defaultShape.getId());
				container.setExtentClass(extentClass);
				container.setSlug(slug);
				app.addContainer(container);
			} else {
				throw new DataAppGeneratorException("Default Shape not found for container: " + pathValue);
			}
			
			
		}
		
		
		
	}
	
	private Shape defaultShape(Path path) {
		
		Operation get = path.getGet();
		if (get != null) {
			ResponseMap responses = get.getResponses();
			if (responses != null) {
				Response response = responses.get("200");
				if (response != null) {
					MediaTypeMap content = response.getContent();
					if (content != null) {
						Iterator<MediaType> sequence = content.values().iterator();
						if (sequence.hasNext()) {
							MediaType mediaType = sequence.next();
							String mediaTypeName = mediaType.stringValue();
							return mediaTypeManager.shapeOfMediaType(mediaTypeName);
						}
					}
				}
				
			}
		}
		return null;
	}


}
