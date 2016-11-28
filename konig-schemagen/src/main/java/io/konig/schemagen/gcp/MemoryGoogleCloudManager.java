package io.konig.schemagen.gcp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.core.Vertex;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class MemoryGoogleCloudManager implements GoogleCloudManager, BigQueryTableHandler {
	private static final List<BigQueryTable> EMPTY = new ArrayList<>();
	
	private Map<String, List<BigQueryTable>> tablesForClass = new HashMap<>();
	private Map<String, GoogleCloudProject> projectMap = new HashMap<>();
	private ShapeManager shapeManager;
	private ProjectMapper projectMapper;
	private DatasetMapper datasetMapper;

	public MemoryGoogleCloudManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}
	
	public MemoryGoogleCloudManager() {
		
	}
	
	
	public MemoryGoogleCloudManager setShapeManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
		return this;
	}

	public ProjectMapper getProjectMapper() {
		return projectMapper;
	}


	@Override
	public void add(GoogleCloudProject project) {
		projectMap.put(project.getProjectId(), project);
	}


	@Override
	public GoogleCloudProject getProjectById(String id) {
		return projectMap.get(id);
	}

	public MemoryGoogleCloudManager setProjectMapper(ProjectMapper projectMapper) {
		this.projectMapper = projectMapper;
		return this;
	}



	public DatasetMapper getDatasetMapper() {
		return datasetMapper;
	}



	public MemoryGoogleCloudManager setDatasetMapper(DatasetMapper datasetMapper) {
		this.datasetMapper = datasetMapper;
		return this;
	}



	@Override
	public Collection<BigQueryTable> tablesForClass(URI owlClass) throws GoogleCloudException {
		Collection<BigQueryTable> result = tablesForClass.get(owlClass.stringValue());
		return result==null ? EMPTY : result;
	}

	@Override
	public void add(BigQueryTable table) {
		
		URI owlClass = table.getTableClass();
		if (owlClass == null) {
			URI shapeId = table.getTableShape();
			if (shapeId != null) {
				Shape shape = shapeManager.getShapeById(shapeId);
				if (shape != null) {
					owlClass = shape.getTargetClass();
				}
			}
		}
		if (owlClass != null) {
			List<BigQueryTable> list = tablesForClass.get(owlClass.stringValue());
			if (list == null) {
				list = new ArrayList<>();
				tablesForClass.put(owlClass.stringValue(), list);
			}
			list.add(table);
		}
		
	}

	@Override
	public BigQueryDataset datasetForClass(Vertex owlClass) throws GoogleCloudException {
		
		if (projectMapper == null) {
			throw new GoogleCloudException("Cannot fetch dataset for class: projectMapper is not defined.");
		}
		if (datasetMapper == null) {
			throw new GoogleCloudException("Cannot fetch dataset for class: datasetMapper is not defined");
		}
		
		BigQueryDataset result = null;
		
		String projectId = projectMapper.projectForClass(owlClass);
		if (projectId != null) {
			String datasetId = datasetMapper.datasetForClass(owlClass);
			if (datasetId != null) {
				GoogleCloudProject project = getProjectById(projectId);
				if (project == null) {
					project = new GoogleCloudProject();
					project.setProjectId(projectId);
					add(project);
				}
				result = project.findProjectDataset(datasetId);
				if (result == null) {
					result = new BigQueryDataset();
					result.setDatasetId(datasetId);
					project.addProjectDataset(result);
				}
				
				
			}
		}
		return result;
	}

	@Override
	public Collection<GoogleCloudProject> listProjects() {
		return projectMap.values();
	}

}
