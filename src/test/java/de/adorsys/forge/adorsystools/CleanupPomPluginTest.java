package de.adorsys.forge.adorsystools;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class CleanupPomPluginTest extends AbstractShellTest
{
   @Deployment
   public static JavaArchive getDeployment()
   {
      return AbstractShellTest.getDeployment()
            .addPackages(true, CleanupPomPlugin.class.getPackage());
   }

   @Test
   public void testDefaultCommand() throws Exception
   {
      getShell().execute("mvn-clean-dep");
   }

   @Test
   public void testCommand() throws Exception
   {
      getShell().execute("mvn-clean-dep command");
   }

   @Test
   public void testPrompt() throws Exception
   {
      queueInputLines("y");
      getShell().execute("mvn-clean-dep prompt foo bar");
   }
}
