package de.adorsys.forge.adorsystools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.PluginManagement;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.maven.dependencies.MavenDependencyAdapter;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.PipeIn;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;

/**
 *
 */
@Alias("cleanup-mvn-pom")
public class CleanupPomPlugin implements Plugin {

	@Inject
	private ShellPrompt prompt;

	@Inject
	private Shell shell;

	@Inject
	private ResourceFactory resourceFactory;

	@DefaultCommand
	public void defaultCommand(@PipeIn String in, PipeOut out) {
		Project currentProject = shell.getCurrentProject();
		final MavenCoreFacet mvnFacet = currentProject.getFacet(MavenCoreFacet.class);


		List<Dependency> dependencyMgmt = new ArrayList<Dependency>();
		Set<org.apache.maven.model.Plugin> pluginMgmt = new HashSet<org.apache.maven.model.Plugin>();

		Properties properties = new Properties();
		cleanUpProject(dependencyMgmt, pluginMgmt, properties);
		mergeDependencies(dependencyMgmt);

		DependencyManagement dependencyManagement = new DependencyManagement();
		dependencyManagement.setDependencies(dependencyMgmt);

		Model pom = mvnFacet.getPOM();
		if (pom.getBuild() == null) {
			pom.setBuild(new Build());
		}
		pom.getBuild().setPluginManagement(new PluginManagement());
		pom.getBuild().getPluginManagement().setPlugins(new ArrayList<org.apache.maven.model.Plugin>(pluginMgmt));
		pom.setDependencyManagement(dependencyManagement);
		pom.setProperties(properties);
		mvnFacet.setPOM(pom);
	}

	private void cleanUpProject(List<Dependency> dependencyMgmt, Collection<org.apache.maven.model.Plugin> pluginMgmt, Properties properties) {
		Project currentProject = shell.getCurrentProject();
		DependencyFacet dependencyFacet = currentProject.getFacet(DependencyFacet.class);
		final MavenCoreFacet mvnFacet = currentProject.getFacet(MavenCoreFacet.class);
		DirectoryResource projectRoot = currentProject.getProjectRoot();
		List<String> modules = mvnFacet.getMavenProject().getModules();
		for (String string : modules) {
			shell.setCurrentResource(projectRoot.getChild(string));
			cleanUpProject(dependencyMgmt, pluginMgmt, properties);
		}
		shell.setCurrentResource(projectRoot);

		Model pom = mvnFacet.getPOM();
		collectAndCleanDependencies(dependencyMgmt, pom);

		collectAndCleanProperties(properties, pom);
		collectAndCleanPluginVersions(pluginMgmt, dependencyFacet, pom);
		mvnFacet.setPOM(pom);
	}

	private void collectAndCleanPluginVersions(Collection<org.apache.maven.model.Plugin> pluginMgmt, DependencyFacet dependencyFacet, Model pom) {
		if (pom.getBuild() != null && pom.getBuild().getPlugins() != null) {
			List<org.apache.maven.model.Plugin> plugins = pom.getBuild().getPlugins();
			for (org.apache.maven.model.Plugin plugin : plugins) {
				org.apache.maven.model.Plugin pluginManaged = new org.apache.maven.model.Plugin();
				pluginManaged.setArtifactId(plugin.getArtifactId());
				pluginManaged.setGroupId(plugin.getGroupId());

				if (plugin.getVersion() == null) {
					List<org.jboss.forge.project.dependencies.Dependency> resolveAvailableVersions = dependencyFacet.resolveAvailableVersions(DependencyBuilder.create()
							.setGroupId(plugin.getGroupId() == null ? "org.apache.maven.plugins": plugin.getGroupId())
							.setArtifactId(plugin.getArtifactId())
							.setVersion("[1.0,)"));
					org.jboss.forge.project.dependencies.Dependency choiceTyped = prompt.promptChoiceTyped(
							"Non uniqe Verions of a dependency. Select a version", resolveAvailableVersions);
					pluginManaged.setVersion(choiceTyped.getVersion());
				} else {
					pluginManaged.setVersion(plugin.getVersion());
				}
				plugin.setVersion(null);
				pluginMgmt.add(pluginManaged);
			}
		}
	}

	private void collectAndCleanProperties(Properties properties, Model pom) {
		if (pom.getProperties() != null) {
			Set<Entry<Object,Object>> entrySet = pom.getProperties().entrySet();
			for (Entry<Object, Object> entry : entrySet) {
				Object oldval = properties.get(entry.getKey());
				if (oldval != null && !oldval.equals(entry.getValue()) &&
						!prompt.promptBoolean("Override " + entry.getKey() + ":" + oldval + " whith " + entry.getValue() , false)) {
					continue;
				}
				properties.put(entry.getKey(), entry.getValue());
			}
			pom.setProperties(null);
		}
	}

	private void mergeDependencies(List<Dependency> dependencies) {
		List<org.jboss.forge.project.dependencies.Dependency> forgeDependencies = MavenDependencyAdapter.fromMavenList(dependencies);
		List<org.jboss.forge.project.dependencies.Dependency> cleanedDependencies = new ArrayList<org.jboss.forge.project.dependencies.Dependency>();

		Map<String, Set<org.jboss.forge.project.dependencies.Dependency>> duplicateDependencies = new HashMap<String, Set<org.jboss.forge.project.dependencies.Dependency>>();
		for (org.jboss.forge.project.dependencies.Dependency a : forgeDependencies) {
			String coordinates = DependencyBuilder.create(a).setVersion(null).toCoordinates();
			Set<org.jboss.forge.project.dependencies.Dependency> deps = duplicateDependencies.get(coordinates);
			if (deps == null) {
				deps = new HashSet<org.jboss.forge.project.dependencies.Dependency>();
				duplicateDependencies.put(coordinates, deps);
			}
			// make a eq hashcode enabled copy
			List<org.jboss.forge.project.dependencies.Dependency> excludedDependencies = a.getExcludedDependencies();
			a = DependencyBuilder.create(a);
			a.getExcludedDependencies().addAll(excludedDependencies);
			deps.add(a);
		}

		for (Entry<String, Set<org.jboss.forge.project.dependencies.Dependency>> entry : duplicateDependencies.entrySet()) {
			if (entry.getValue().size() == 1) {
				cleanedDependencies.add(entry.getValue().iterator().next());
			} else {
				org.jboss.forge.project.dependencies.Dependency choised = prompt.promptChoiceTyped(
						"Non uniqe Verions of a dependency. Select a version",
						new ArrayList<org.jboss.forge.project.dependencies.Dependency>(entry.getValue()));
				cleanedDependencies.add(choised);
			}
		}
		dependencies.clear();
		dependencies.addAll(MavenDependencyAdapter.toMavenList(cleanedDependencies));
	}

	public void collectAndCleanDependencies(List<Dependency> dependencyMgmt, Model model) {
		Iterator<Dependency> iterator = model.getDependencies().iterator();
		while (iterator.hasNext()) {
			Dependency dependency = iterator.next();
			String version = dependency.getVersion();
			if (version != null) {
				dependencyMgmt.add(dependency.clone());
				dependency.setVersion(null);
				dependency.setExclusions(null);
			}
		}
		if (model.getDependencyManagement() != null) {
			dependencyMgmt.addAll(model.getDependencyManagement().getDependencies());
			model.setDependencyManagement(null);
		}
	}


}
