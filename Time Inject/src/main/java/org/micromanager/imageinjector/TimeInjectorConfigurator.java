
package org.micromanager.imageinjector;

import javax.swing.*;

import org.micromanager.PropertyMap;
import org.micromanager.PropertyMaps;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.internal.utils.WindowPositioning;


public class TimeInjectorConfigurator extends JFrame implements ProcessorConfigurator {


   private final Studio studio_;


   public TimeInjectorConfigurator(Studio studio) {
      studio_ = studio;
      initComponents();
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);

   }


   @Override
   public void showGUI() { }

   @Override
   public void cleanup() {
      dispose();
   }

   /** This method is called from within the constructor to
    * initialize the form.
    */
   private void initComponents() {
      pack();
   }

    private void slicesCheckBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mirrorCheckBox_ActionPerformed
       studio_.data().notifyPipelineChanged();
    }

   private void slicesComboBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateComboBox_ActionPerformed
      studio_.data().notifyPipelineChanged();
   }

   private void channelComboBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cameraComboBox_ActionPerformed
      studio_.data().notifyPipelineChanged();
   }

   @Override
   public PropertyMap getSettings() {
      PropertyMap.Builder builder = PropertyMaps.builder();
      return builder.build();
   }
}