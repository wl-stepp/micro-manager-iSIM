
package org.micromanager.imageinjector;

import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorPlugin;
import org.micromanager.data.ProcessorFactory;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

@Plugin(type = ProcessorPlugin.class)
public class TimeInjectorPlugin implements ProcessorPlugin, SciJavaPlugin {
   private Studio studio_;


   @Override
   public void setContext(Studio studio) {
      studio_ = studio;
   }

   @Override
   public ProcessorConfigurator createConfigurator(PropertyMap properties) {
      return new TimeInjectorConfigurator(studio_);
   }

   @Override
   public ProcessorFactory createFactory(PropertyMap settings) {
            return new TimeInjectorFactory(studio_);
   }

   @Override
   public String getName() {
      return "Time Injector";
   }

   @Override
   public String getHelpText() {
      return "Injects the received time as elapsed time";
   }

   @Override
   public String getVersion() {
      return "Version 1.0";
   }

   @Override
   public String getCopyright() {
      return "Copyright Willi Stepp EPFL, 2021";
   }

   public ProcessorConfigurator getConfigurator() {
      return createConfigurator(null);
   }

}
