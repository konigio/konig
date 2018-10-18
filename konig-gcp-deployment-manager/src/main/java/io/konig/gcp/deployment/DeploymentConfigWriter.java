package io.konig.gcp.deployment;

import java.io.Writer;

import io.konig.yaml.AnchorFeature;
import io.konig.yaml.YamlWriter;

public class DeploymentConfigWriter {
	
	public void write(Writer out, DeploymentConfig config) throws Exception {
		YamlWriter yaml = new YamlWriter(out);
		yaml.setAnchorFeature(AnchorFeature.NONE);
		yaml.setIncludeClassTag(false);
		yaml.write(config);
		yaml.close();
	}

}
