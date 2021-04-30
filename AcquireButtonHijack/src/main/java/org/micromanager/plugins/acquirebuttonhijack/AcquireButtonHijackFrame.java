/**
 * ExampleFrame.java
 *
 * This module shows an example of creating a GUI (Graphical User Interface).
 * There are many ways to do this in Java; this particular example uses the
 * MigLayout layout manager, which has extensive documentation online.
 *
 *
 * Nico Stuurman, copyright UCSF, 2012, 2015
 *
 * LICENSE: This file is distributed under the BSD license. License text is
 * included with the source distribution.
 *
 * This file is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.
 *
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
 */
package org.micromanager.plugins.acquirebuttonhijack;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;


import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.dialogs.AcqControlDlg;
import org.micromanager.internal.dialogs.*;

import org.micromanager.Studio;






// Imports for MMStudio internal packages
// Plugins should not access internal packages, to ensure modularity and
// maintainability. However, this plugin code is older than the current
// MMStudio API, so it still uses internal classes and interfaces. New code
// should not imitate this practice.


public class AcquireButtonHijackFrame extends JFrame {

   private Studio studio_;
   private AcqControlDlg acw_;

   public AcquireButtonHijackFrame(Studio studio) {
      super("AcquireButtonHijack");
      studio_ = studio;
      // Get the Acquire! Button and strip of the functionality, so we can put something of our own.
      acw_ = ((MMStudio) studio_).uiManager().getAcquisitionWindow();
      JPanel panel0 = (JPanel) acw_.getContentPane().getComponent(0);
      JPanel panel1 =(JPanel) panel0.getComponent(2);
      JPanel panel2 = (JPanel) panel1.getComponent(1);
      JButton button = (JButton) panel2.getComponent(0);
      button.setText("Acquire");
      ActionListener[] listeners = button.getActionListeners();
      for (ActionListener listener : listeners){
         button.removeActionListener(listener);
         System.out.println(listener.toString());
      }

      // Increase the number of frames and turn channels of if channels is selected and then start acquisition
      button.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            Thread acq = new acqThread();
            acq.start();
         }
      });

   }

   public class acqThread extends Thread{
      public void run(){
         System.out.println("ACQ running");
         // Call applySettingsfromGUI with a small trick
         PropertyChangeEvent e = new PropertyChangeEvent(1, "test",0,1);
         acw_.propertyChange(e);
         SequenceSettings settings = studio_.acquisitions().getAcquisitionSettings();
         if (settings.useChannels()){
            SequenceSettings new_settings = settings.copyBuilder().useChannels(false).numFrames(settings.numFrames()*2).intervalMs(0).build();
            studio_.acquisitions().runAcquisitionWithSettings(new_settings,false);
            studio_.acquisitions().setAcquisitionSettings(settings);
         } else {
            SequenceSettings new_settings = settings.copyBuilder().intervalMs(0).build();
            studio_.acquisitions().runAcquisitionWithSettings(new_settings,false);
            studio_.acquisitions().setAcquisitionSettings(settings);
         }
      }
   }

}



