
package org.micromanager.pseudochannels;

import org.micromanager.PropertyMaps;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorPlugin;
import org.micromanager.data.ProcessorFactory;
import org.micromanager.data.DataManager;

import java.util.List;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import java.io.IOException;

@Plugin(type = ProcessorPlugin.class)
public class PseudoChannelPlugin implements ProcessorPlugin, SciJavaPlugin {
   private Studio studio_;
   private PseudoChannelFactory factory_;
   private DataManager data_;

   @Override
   public void setContext(Studio studio) {
      studio_ = studio;
   }

   @Override
   public ProcessorConfigurator createConfigurator(PropertyMap settings) {
      return new PseudoChannelConfigurator(studio_, settings);
   }

   @Override
   public ProcessorFactory createFactory(PropertyMap settings) {
      factory_ = new PseudoChannelFactory(settings, studio_);
      return factory_;
   }

   @Override
   public String getName() {
      return "Pseudo Channels";
   }

   @Override
   public String getHelpText() {
      return "Puts images into Pseudo Channels";
   }

   @Override
   public String getVersion() {
      return "Version 1.0";
   }

   @Override
   public String getCopyright() {
      return "Copyright Willi Stepp EPFL, 2021";
   }

   public ProcessorConfigurator getConfigurator(int channels, int slices, boolean useSlices) {
      PropertyMap.Builder builder = PropertyMaps.builder();
      builder.putInteger("channels", channels);
      builder.putString("slices", String.valueOf(slices));
      builder.putBoolean("useSlices", useSlices);
      System.out.println(builder.build());
      return createConfigurator(builder.build());
   }

}
