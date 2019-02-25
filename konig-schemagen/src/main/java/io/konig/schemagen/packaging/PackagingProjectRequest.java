package io.konig.schemagen.packaging;

import java.io.File;
import java.util.List;

public class PackagingProjectRequest {

	private File basedir;
	private MavenProject mavenProject;
	private List<ResourceKind> resourceKind;
	private File velocityLogFile;
	
	public File getBasedir() {
		return basedir;
	}
	public void setBasedir(File basedir) {
		this.basedir = basedir;
	}
	public MavenProject getMavenProject() {
		return mavenProject;
	}
	public void setMavenProject(MavenProject mavenProject) {
		this.mavenProject = mavenProject;
	}
	public List<ResourceKind> getResourceKind() {
		return resourceKind;
	}
	public void setResourceKind(List<ResourceKind> resourceKind) {
		this.resourceKind = resourceKind;
	}
	public File getVelocityLogFile() {
		return velocityLogFile;
	}
	public void setVelocityLogFile(File velocityLogFile) {
		this.velocityLogFile = velocityLogFile;
	}
	
	

	
}
