package org.micromanager.plugins.pythoneventserver;

import org.micromanager.MenuPlugin;
import org.micromanager.Studio;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

/**
 * ZMQ based Plugin that relays Micro-Manager events to python
 * <p>
 * This is used as a MenuPlugin in Micro-Manager and calls PythonEventServerFrame.
 * More description of how this works there. This just implements the necessary
 * methods for being recognized as a Plugin by Micro-Manager.
 * <p>
 *
 * @author Willi Stepp
 * @version 0.1
 */

@Plugin(type = MenuPlugin.class)
public class PythonEventServer implements SciJavaPlugin, MenuPlugin {
   private Studio studio_;
   private PythonEventServerFrame frame_;

   /**
    * This method receives the Studio object, which is the gateway to the
    * Micro-Manager API. You should retain a reference to this object for the
    * lifetime of your plugin. This method should not do anything except for
    * store that reference, as Micro-Manager is still busy starting up at the
    * time that this is called.
    */
   @Override
   public void setContext(Studio studio) {
      studio_ = studio;
   }

   @Override
   public void onPluginSelected() {
      if (frame_ == null) {
         // We have never before shown our GUI, so now we need to create it.
         frame_ = new PythonEventServerFrame(studio_);
      } else {
         frame_.dispose();
         frame_ = new PythonEventServerFrame(studio_);
      }
      frame_.setVisible(true);
   }

   /**
    * This string is the sub-menu that the plugin will be displayed in, in the
    * Plugins menu.
    */
   @Override
   public String getSubMenu() {
      return "Developer Tools";
   }

   /**
    * The name of the plugin in the Plugins menu.
    */
   @Override
   public String getName() {
      return "Python Event Server";
   }

   @Override
   public String getHelpText() {
      return "This plugin starts a ZMQ Server in a new thread that can be interacted with from python.";
   }

   @Override
   public String getVersion() {
      return "0.1";
   }

   @Override
   public String getCopyright() {
      return "Willi Stepp EPFL, 2021";
   }
}
