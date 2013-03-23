package de.adorsys.forge.adorsystools;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class ReleaseSetupPluginTest extends AbstractShellTest {
	@Deployment
	public static JavaArchive getDeployment() {
		return AbstractShellTest.getDeployment().addPackages(true, ReleaseSetupPlugin.class.getPackage());
	}

	@Test
	public void testDefaultCommand() throws Exception {
		Project p = initializeJavaProject();
		System.out.println(p.getProjectRoot().getUnderlyingResourceObject().getAbsolutePath());
		getShell().execute("release-setup --git-repo-url https://fisheye.adorsys.de/git/nrs-produktionsdaten.git " +
				"--git-browser-url https://fisheye.adorsys.de/browse/nrs-produktionsdaten " +
				"--mvn-release-repo-id adorsys.releases --mvn-release-repo https://maven.prod.adorsys.de/nexus/content/repositories/releases/ " +
				"--mvn-snapshot-repo-id adorsys.snapshots --mvn-snapshot-repo https://maven.prod.adorsys.de/nexus/content/repositories/snapshots/");
	}

}
