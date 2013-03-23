package de.adorsys.forge.adorsystools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Scm;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeIn;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;

/**
 * 
 * @author sso
 */
@Alias("release-setup")
public class ReleaseSetupPlugin implements Plugin {

	private static final String UTF_8 = "UTF-8";

	@Inject
	private ShellPrompt prompt;

	@Inject
	private Project project;

	@DefaultCommand
	public void setup(PipeOut out,
			@Option(name = "git-repo-url", required=true) String gitRepoUrl,
			@Option(name = "git-browser-url", required=true) String gitBrowserUrl,
			@Option(name = "mvn-release-repo-id", required=true) String mvnReleaseRepoId,
			@Option(name = "mvn-release-repo", required=true) String mvnReleaseRepo,
			@Option(name = "mvn-snapshot-repo-id", required=true) String mvnSnapshotRepoId,
			@Option(name = "mvn-snapshot-repo", required=true) String mvnSnapshotRepo
			) {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		Scm scm = new Scm();
		String scmUrl = "scm:git:"+ gitRepoUrl;
		scm.setConnection(scmUrl);
		scm.setDeveloperConnection(scmUrl);
		scm.setUrl(gitBrowserUrl);
		pom.setScm(scm);

		DistributionManagement distributionManagement = new DistributionManagement();
		DeploymentRepository releaseRepository = new DeploymentRepository();
		releaseRepository.setId(mvnReleaseRepoId);
		releaseRepository.setUrl(mvnReleaseRepo);
		distributionManagement.setRepository(releaseRepository);

		DeploymentRepository snapshotRepository = new DeploymentRepository();
		snapshotRepository.setId(mvnSnapshotRepoId);
		snapshotRepository.setUrl(mvnSnapshotRepo);
		distributionManagement.setSnapshotRepository(snapshotRepository);
		pom.setDistributionManagement(distributionManagement);

		org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();

		plugin.setArtifactId("maven-release-plugin");
		plugin.setVersion("2.2.2");
		Xpp3Dom dom;
		try {
			dom = Xpp3DomBuilder
					.build(new ByteArrayInputStream(
							("<configuration><autoVersionSubmodules>true</autoVersionSubmodules></configuration>")
							.getBytes()), UTF_8);
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		plugin.setConfiguration(dom);
		pom.getBuild().getPlugins().add(plugin);

		addRepository(pom, "JBOSS_NEXUS", "http://repository.jboss.org/nexus/content/groups/public");
		mvnFacet.setPOM(pom);
	}

	/**
	 * Add a repository to pom
	 * @param repoId
	 * @param repoUrl
	 */
	void addRepository(Model pom, String repoId, String repoUrl) {
		Repository mvp4gRepo = new Repository();
		mvp4gRepo.setId(repoId);
		mvp4gRepo.setUrl(repoUrl);
		List<Repository> repositories = pom.getRepositories();
		for (Repository repository : repositories) {
			if (repoId.equals(repository.getId())){
				//noop
				return;
			}
		}
		repositories.add(mvp4gRepo);
	}

	@Command
	public void command(@PipeIn String in, PipeOut out, @Option String... args) {
		if (args == null) {
			out.println("Executed named command without args.");
		} else {
			out.println("Executed named command with args: " + Arrays.asList(args));
		}
	}

	@Command
	public void prompt(@PipeIn String in, PipeOut out) {
		if (prompt.promptBoolean("Do you like writing Forge plugins?")) {
			out.println("I am happy.");
		} else {
			out.println("I am sad.");
		}
	}
}
