package io.konig.project.mgmt;

import java.util.Collection;
import java.util.HashSet;

import org.openrdf.model.Resource;

import io.konig.annotation.InverseOf;
import io.konig.schemagen.gcp.BigQueryDataset;

public class KonigProject {
	
	private Resource id;
	private String projectName;
	private Collection<GooglePubSubTopic> projectTopic;
	private Collection<BigQueryDataset> projectDataset;
	
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public Collection<GooglePubSubTopic> getProjectTopic() {
		return projectTopic;
	}
	public void setProjectTopic(Collection<GooglePubSubTopic> projectTopic) {
		this.projectTopic = projectTopic;
	}

	@InverseOf("http://www.konig.io/ns/core/topicProject")
	public void addProjectTopic(GooglePubSubTopic topic) {
		if (projectTopic == null) {
			projectTopic = new HashSet<>();
		}
		
		projectTopic.add(topic);
	}
	public Resource getId() {
		return id;
	}
	public void setId(Resource id) {
		this.id = id;
	}
	
	public int hashCode() {
		return id==null ? 0 : id.stringValue().hashCode();
	}
	
	public boolean equals(Object object) {
		if (object instanceof KonigProject) {
			KonigProject other = (KonigProject) object;
			return id != null && id.equals(other.getId());
		}
		return false;
	}
	
	public Collection<BigQueryDataset> getProjectDataset() {
		return projectDataset;
	}
	
	public void setProjectDataset(Collection<BigQueryDataset> projectDataset) {
		this.projectDataset = projectDataset;
	}
	
	public void addProjectDataset(BigQueryDataset dataset) {
		if (projectDataset == null) {
			projectDataset = new HashSet<>();
		}
		projectDataset.add(dataset);
	}
	
	

	
}
