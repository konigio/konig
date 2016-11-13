package io.konig.project.mgmt;

import java.util.ArrayList;
import java.util.Collection;

import org.openrdf.model.URI;

import io.konig.annotation.InverseOf;
import io.konig.schemagen.gcp.GoogleCloudProject;
import io.konig.shacl.Shape;

/**
 * Metadata about a Google PubSub topic
 * @author Greg McFall
 *
 */
public class GooglePubSubTopic {

	private URI id;
	private String topicName;
	private String description;
	private URI jsonTemplate;
	private KonigProject topicProject;
	
	private Collection<Shape> acceptsShape;
	
	
	/**
	 * A URI for the PubSub topic independent of any particular
	 * environment.  This is NOT the URL where you manage the
	 * PubSub topic in Google Cloud Platform; it is merely an identifier
	 * for this logical resource.
	 * @return
	 */
	public URI getId() {
		return id;
	}

	public void setId(URI id) {
		this.id = id;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicCodeName) {
		this.topicName = topicCodeName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * The set of data Shapes for messages that can be published to
	 * the Topic.
	 * @return
	 */
	public Collection<Shape> getAcceptsShape() {
		return acceptsShape;
	}
	
	public void addAcceptsShape(Shape shape) {
		if (acceptsShape == null) {
			acceptsShape = new ArrayList<>();
		}
		acceptsShape.add(shape);
	}

	public void setAcceptsShape(Collection<Shape> acceptsShape) {
		this.acceptsShape = acceptsShape;
	}

	public URI getJsonTemplate() {
		return jsonTemplate;
	}

	public void setJsonTemplate(URI jsonTemplate) {
		this.jsonTemplate = jsonTemplate;
	}

	public KonigProject getTopicProject() {
		return topicProject;
	}

	@InverseOf("http://www.konig.io/ns/core/projectTopic")
	public void setTopicProject(KonigProject topicProject) {
		this.topicProject = topicProject;
	}
	
	public int hashCode() {
		return id==null ? 0 : id.stringValue().hashCode();
	}

	public boolean equals(Object object) {
		if (object instanceof GooglePubSubTopic) {
			GooglePubSubTopic other = (GooglePubSubTopic) object;
			return id != null && id.equals(other.getId());
		}
		return false;
	}
}
