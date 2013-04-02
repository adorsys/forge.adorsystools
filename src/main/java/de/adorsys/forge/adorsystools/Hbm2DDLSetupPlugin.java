package de.adorsys.forge.adorsystools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.PipeIn;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.seam.render.TemplateCompiler;
import org.jboss.seam.render.template.CompiledTemplateResource;

/**
 * 
 * @author sso
 */
@Alias("hbm2ddl-setup")
public class Hbm2DDLSetupPlugin implements Plugin {
	private static final String ORG_HIBERNATE = "org.hibernate";

	private static final String HBM_VERSION = "3.5.1-Final";

	private static final String UTF_8 = "UTF-8";

	@Inject
	private ShellPrompt prompt;

	@Inject
	private Project project;

	private final CompiledTemplateResource databasePropsCompiler;

	@Inject
	public Hbm2DDLSetupPlugin(TemplateCompiler compiler) {
		databasePropsCompiler = compiler.compileResource(Hbm2DDLSetupPlugin.class.getResourceAsStream("/templates/database.properties.jv"));

	}

	@DefaultCommand
	public void defaultCommand(@PipeIn String in, PipeOut out) {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		ResourceFacet resourceFacet = project.getFacet(ResourceFacet.class);

		createDatabaseProperties(resourceFacet);

		Model pom = mvnFacet.getPOM();

		org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();

		plugin.setGroupId("org.codehaus.mojo");
		plugin.setArtifactId("hibernate3-maven-plugin");
		plugin.setVersion("2.2");

		if (pom.getBuild().getPlugins().contains(plugin)) {
			return;
		}
		Xpp3Dom dom;
		try {
			String configuration = "<configuration>" + "<components>" + "<component>" + "<name>hbm2ddl</name>"
					+ "<implementation>jpaconfiguration</implementation>" + "</component>" + "</components>" + "<componentProperties>"
					+ "<persistenceunit>default</persistenceunit>" + "<console>true</console>" + "<drop>true</drop>"
					+ "<create>true</create>" + "<format>true</format>" + "<export>false</export>"
					+ "<propertyfile>src/main/resources/database.properties</propertyfile>" + "<outputfilename>schema.ddl</outputfilename>"
					+ "</componentProperties>" + "</configuration>";
			dom = Xpp3DomBuilder.build(new ByteArrayInputStream((configuration).getBytes()), UTF_8);
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		plugin.setConfiguration(dom);
		List<Dependency> dependencies = new ArrayList<Dependency>();
		{
			Dependency dependency = new Dependency();
			dependency.setGroupId(ORG_HIBERNATE);
			dependency.setArtifactId("hibernate-annotations");
			dependency.setVersion(HBM_VERSION);
			dependencies.add(dependency);
		}
		{
			Dependency dependency = new Dependency();
			dependency.setGroupId(ORG_HIBERNATE);
			dependency.setArtifactId("ejb3-persistence");
			dependency.setVersion(HBM_VERSION);
			dependencies.add(dependency);
		}
		{
			Dependency dependency = new Dependency();
			dependency.setGroupId(ORG_HIBERNATE);
			dependency.setArtifactId("hibernate-tools");
			dependency.setVersion(HBM_VERSION);
			dependencies.add(dependency);
		}
		{
			Dependency dependency = new Dependency();
			dependency.setGroupId(ORG_HIBERNATE);
			dependency.setArtifactId("hibernate-core");
			dependency.setVersion(HBM_VERSION);
			dependencies.add(dependency);
		}
		{
			Dependency dependency = new Dependency();
			dependency.setGroupId(ORG_HIBERNATE);
			dependency.setArtifactId("hibernate-validator");
			dependency.setVersion(HBM_VERSION);
			dependencies.add(dependency);
		}
		plugin.setDependencies(dependencies);

		pom.getBuild().getPlugins().add(plugin);

		mvnFacet.setPOM(pom);
	}

	@SuppressWarnings("unchecked")
	private void createDatabaseProperties(ResourceFacet resourceFacet) {
		String render = databasePropsCompiler.render(new HashMap<Object, Object>());

		resourceFacet.getResourceFolder().getChildOfType(FileResource.class, "database.properties").setContents(render);
	}

}
