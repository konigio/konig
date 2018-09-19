package io.konig.maven.invoker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.maven.cli.MavenCli;

public class MojoRuntime {
	
	private String groupId;
	private String artifactId;
	private String version;
	private String goal;
	
	private XmlElement xml;
	private File basedir;
	
	private PrintStream out;
	private PrintStream err;
	
	
	public MojoRuntime() {
		
	}
	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getGoal() {
		return goal;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}

	public XmlElement getXml() {
		return xml;
	}

	public void setXml(XmlElement xml) {
		this.xml = xml;
	}

	public File getBasedir() {
		return basedir;
	}

	public void setBasedir(File basedir) {
		this.basedir = basedir;
	}
	
	

	public PrintStream getOut() {
		return out;
	}

	public void setOut(PrintStream out) {
		this.out = out;
	}

	public PrintStream getErr() {
		return err;
	}

	public void setErr(PrintStream err) {
		this.err = err;
	}

	private String hash(XmlElement e) throws NoSuchAlgorithmException {
		String text = e.toString();
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(text.getBytes());
		byte[] digest = md.digest();
		String encoded = Base64.getEncoder().encodeToString(digest).replace('/', '-').replace('=','_');
		return encoded.length()>16 ? encoded.substring(0, 16) : encoded;
	}



	public static Builder builder() {
		return new Builder();
	}
	
	public int execute() throws  MojoRuntimeException {

		try {
			if (out==null) {
				out = System.out;
			}
			if (err == null) {
				err = System.err;
			}
			if (basedir == null) {
				String hash = hash(xml);
				basedir = new File("target/mojo-runtime/" + groupId + "/" + artifactId + "/" + version + "/" + hash);
			}
			deleteAll(basedir);
			
			writePom();
			
			
			MavenCli maven = new MavenCli();
			
			StringBuilder sb = new StringBuilder();
			sb.append(groupId);
			sb.append(':');
			sb.append(artifactId);
			sb.append(':');
			sb.append(version);
			sb.append(':');
			sb.append(goal);
			
			String fullGoal = sb.toString();
			
			return maven.doMain(new String[]{fullGoal}, basedir.getAbsolutePath(), out, err);
			
			
		} catch (Throwable oops) {
			throw new MojoRuntimeException(oops);
		}
	}

	private void writePom() throws IOException {

		basedir.mkdirs();
		File pom = new File(basedir, "pom.xml");
		try (
			FileWriter writer = new FileWriter(pom); 
			PrettyPrintWriter out = new PrettyPrintWriter(writer)
		) {
			xml.print(out);
		}
		
	}

	private void deleteAll(File dir) {
		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				if (file.isDirectory()) {
					deleteAll(file);
				}
				file.delete();
			}
		}
	}

	public static class Builder {
		private String modelVersion = "4.0.0";
		private String groupId;
		private String artifactId;
		private String version;
		private XmlElement configuration;
		private String goal;
		private File basedir;
		private PrintStream out;
		private PrintStream err;
		
		private List<ElementBuilder> stack = new ArrayList<>();
		public Builder() {
		}
		
		public MojoRuntime build() {
			
			if (groupId==null) {
				throw new MojoBuildException("groupId must be defined");
			}
			if (artifactId == null) {
				throw new MojoBuildException("artifactId must be defined");
			}
			if (version == null) {
				throw new MojoBuildException("version must be defined");
			}
			if (goal == null) {
				throw new MojoBuildException("goal must be defined");
			}

			XmlElement xml = new XmlElement("project");
			new ElementBuilder(xml)
				.attribute("xmlNamespace","http://maven.apache.org/POM/4.0.0")
				.attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
				.attribute("xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd")
				.property("modelVersion", modelVersion)
				.property("groupId", "io.konig.mojoruntime")
				.property("artifactId", artifactId + "-runner")
				.property("version", "1.0.0")
				.begin("build")
					.begin("plugins")
						.begin("plugin")
							.property("groupId", groupId)
							.property("artifactId", artifactId)
							.property("version", version)
							.append(configuration)
					.end("plugins")
				.end("build")
				;
				
			
			MojoRuntime runtime = new MojoRuntime();
			runtime.setGroupId(groupId);
			runtime.setArtifactId(artifactId);
			runtime.setVersion(version);
			runtime.setBasedir(basedir);
			runtime.setXml(xml);
			runtime.setGoal(goal);
			runtime.setOut(out);
			runtime.setErr(err);
			
			return runtime;
		}
		
		public Builder basedir(File basedir) {
			this.basedir = basedir;
			return this;
		}
		
		public Builder out(PrintStream out) {
			this.out = out;
			return this;
		}
		
		public Builder err(PrintStream err) {
			this.err = err;
			return this;
		}
		
		public Builder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}
		
		public Builder modelVersion(String modelVersion) {
			this.modelVersion = modelVersion;
			return this;
		}
		
		public Builder artifactId(String artifactId) {
			this.artifactId = artifactId;
			return this;
		}
		
		public Builder version(String version) {
			this.version = version;
			return this;
		}
		
		public Builder goal(String goal) {
			this.goal = goal;
			return this;
		}
		
		public ElementBuilder beginConfiguration() {
			ElementBuilder e = new ElementBuilder(new XmlElement("configuration"));
			push(e);
			return e;
		}
		
		
		
		private void push(ElementBuilder e) {
			stack.add(e);
		}
		
		private ElementBuilder pop() {
			return stack.isEmpty() ? null : stack.remove(stack.size()-1);
		}
		
		private ElementBuilder peek() {
			return stack.isEmpty() ? null : stack.get(stack.size()-1);
		}
		
		
		
		public class ElementBuilder {
			XmlElement element;

			public ElementBuilder(XmlElement element) {
				this.element = element;
			}
			
			public ElementBuilder attribute(String name, String value) {
				element.addAttribute(new XmlAttribute(name, value));
				return this;
			}
			
			private ElementBuilder append(XmlNode child) {
				if (child != null) {
					element.addNode(child);
				}
				return this;
			}
			
			public ElementBuilder property(String name, File value) {
				return this.property(name, value.getPath().replace('\\','/'));
			}
			
			public ElementBuilder property(String name, String value) {
				XmlElement child = new XmlElement(name);
				child.addNode(new XmlText(value));
				return this;
			}
			
			public ElementBuilder begin(String tagName) {
				XmlElement child = new XmlElement(tagName);
				
				element.addNode(child);
				
				ElementBuilder next = new ElementBuilder(child);
				push(next);
				return next;
			}
			
			public ElementBuilder end(String tagName) {
				for (
						ElementBuilder builder = pop(); 
						builder != null && !builder.element.getTagName().equals(tagName); 
						builder = pop()
				);
				return peek();
			}
			
			public Builder endConfiguration() {
				return Builder.this;
			}
			
		}
		
		
		
	}
	
	
}
