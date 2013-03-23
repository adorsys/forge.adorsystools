package de.adorsys.forge.adorsystools;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class AddMavenPluginPluginTest extends AbstractShellTest {
	@Deployment
	public static JavaArchive getDeployment() {
		return AbstractShellTest.getDeployment().addPackages(true, AddMavenPluginPlugin.class.getPackage());
	}

	@Test
	public void testAS7() throws Exception {
		initializeJavaProject();
		getShell().execute("add-mvn-plugin as7");
	}

	@Test
	public void testCustom() throws Exception {
		initializeJavaProject();
		getShell().execute("add-mvn-plugin custom --artifact jboss-as-maven-plugin --group org.jboss.as.plugins --version 7.1.1.Final");
	}

}
