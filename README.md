forge.adorsystools
==================

forge commands to setup maven release properties, ddl generation (hbm2ddl) and jboss deployment

Installation
============

forge git-plugin https://github.com/adorsys/forge.adorsystools

Cleanup Dependency and Plugin-Verions in a Maven-Multi-Module Project
=====================================================================

This command moves dependeny and plugin-versions to the DependencyManagement oder PluginManagement Section of the root POM

cleanup-mvn-pom

Setup Release and Distribution Management
=========================================

This plugin creates necessary configurations like distibution-management or SCM configuration for the mvn release process.

release-setup --git-repo-url https://fisheye.adorsys.de/git/nrs-produktionsdaten.git --git-browser-url https://fisheye.adorsys.de/browse/nrs-produktionsdaten --mvn-release-repo-id adorsys.releases --mvn-release-repo https://maven.prod.adorsys.de/nexus/content/repositories/releases/ --mvn-snapshot-repo-id adorsys.snapshots --mvn-snapshot-repo https://maven.prod.adorsys.de/nexus/content/repositories/snapshots/ ;
