package de.adorsys.forge.adorsystools;

import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;

/**
 * 
 * @author sso
 */
@Alias("add-mvn-plugin")
public class AddMavenPluginPlugin implements Plugin {
	@Inject
	private ShellPrompt prompt;

	@Inject
	private Project project;

	@Command
	public void custom(PipeOut out, @Option(name = "artifact", required = true) String artifact,
			@Option(name = "group", required = true) String group, @Option(name = "version", required = true) String version) {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);

		Model pom = mvnFacet.getPOM();

		org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();

		plugin.setGroupId(group);
		plugin.setArtifactId(artifact);
		plugin.setVersion(version);

		if (pom.getBuild().getPlugins().contains(plugin)) {
			return;
		}

		pom.getBuild().getPlugins().add(plugin);

		mvnFacet.setPOM(pom);

	}

	@Command
	public void as7(PipeOut out) {
		custom(out, "jboss-as-maven-plugin", "org.jboss.as.plugins", "7.1.1.Final");

	}
}
