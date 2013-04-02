package de.adorsys.forge.adorsystools;

import java.io.File;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class Hbm2DDLSetupPluginTest extends AbstractShellTest
{
	@Deployment
	public static JavaArchive getDeployment()
	{
		return AbstractShellTest.getDeployment()
				.addPackages(true, Hbm2DDLSetupPlugin.class.getPackage());
	}

	@Test
	public void testDefaultCommand() throws Exception
	{
		Project p = initializeJavaProject();
		getShell().execute("hbm2ddl-setup");
		Assert.assertTrue(new File(p.getProjectRoot().getUnderlyingResourceObject(), "src/main/resources/database.properties").exists());

	}
}
