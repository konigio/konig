package io.konig.test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

import groovy.lang.GroovyShell;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GcpDeploymentTest {

	@Test
	public void test1GcpMultiProject() throws Exception {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File("src/integration-test/resources/gcp-multi-project/pom.xml"));
		request.setGoals(Collections.singletonList("install"));
		DefaultInvoker invoker = new DefaultInvoker();
		InvocationResult result = invoker.execute(request);
		assertTrue(result.getExitCode() != 0 ? result.getExecutionException().toString() : "Success",
				result.getExitCode() == 0);
	}

	@Test
	public void test2DemoProject() throws MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File("src/integration-test/resources/demo/demo-parent/pom.xml"));
		request.setGoals(Collections.singletonList("install"));
		DefaultInvoker invoker = new DefaultInvoker();
		InvocationResult result = invoker.execute(request);
		assertTrue(result.getExitCode() != 0 ? result.getExecutionException().toString() : "Success",
				result.getExitCode() == 0);
	}

	@Ignore
	public void test3Script() throws CompilationFailedException, IOException {
		File file = new File(
				"src/integration-test/resources/demo/demo-gcp-model/target/generated/gcp/scripts/deploy.groovy");
		assertTrue(file.exists());
		GroovyShell shell = new GroovyShell();
		shell.evaluate(file);

	}
}
