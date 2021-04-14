///////////////////////////////////////////////////////////////////////////////
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Chris Weisiger
//
// COPYRIGHT:    University of California, San Francisco, 2011, 2015
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.pseudochannels;

import com.bulenkov.iconloader.IconLoader;

import ij.process.ByteProcessor;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

import org.micromanager.PropertyMap;
import org.micromanager.PropertyMaps;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.Coordinates;
import org.micromanager.internal.utils.WindowPositioning;
import org.micromanager.propertymap.MutablePropertyMapView;

// Imports for MMStudio internal packages
// Plugins should not access internal packages, to ensure modularity and
// maintainability. However, this plugin code is older than the current
// MMStudio API, so it still uses internal classes and interfaces. New code
// should not imitate this practice.

public class PseudoChannelConfigurator extends JFrame implements ProcessorConfigurator {

   private static final String DEFAULT_MIRRORED = "Whether or not to mirror the image flipper";
   private static final String DEFAULT_ROTATION = "How much to rotate the image flipper";
   private static final String C1 = "1";
   private static final String C2 = "2";
   private static final String C3 = "3";
   private static final String C4 = "4";
   private static final String[] RS = {C1, C2, C3, C4};
   private static final List<Integer> R_INTS =
      Arrays.asList(new Integer[] {PseudoChannelProcessor.C1,
         PseudoChannelProcessor.C2, PseudoChannelProcessor.C3, PseudoChannelProcessor.C4});
   private static final String EXAMPLE_ICON_PATH =
                        "/org/micromanager/icons/R.png";

   private final int frameXPos_ = 300;
   private final int frameYPos_ = 300;

   private final Studio studio_;
   private final MutablePropertyMapView defaults_;

   private javax.swing.JComboBox channelComboBox_;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabelchannel;
   private javax.swing.JCheckBox slicesCheckBox_;
   private javax.swing.JTextField slicesTextField_;

   public PseudoChannelConfigurator(Studio studio, PropertyMap settings) {
      studio_ = studio;
      defaults_ = studio_.profile().getSettings(this.getClass());

      initComponents();

      Boolean useSlices = settings.getBoolean("useSlices",
            defaults_.getBoolean(DEFAULT_MIRRORED, false));
      Integer rotation = settings.getInteger("channels",
            defaults_.getInteger(DEFAULT_ROTATION, PseudoChannelProcessor.C1));

      slicesCheckBox_.setSelected(useSlices);
      slicesTextField_.setText(settings.getString("slices", "1"));
      channelComboBox_.setSelectedIndex(settings.getInteger("channels", 1)-1);

      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
              getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(frameXPos_, frameYPos_);
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);

   }

   @Override
   public void showGUI() {
      setVisible(true);
   }

   @Override
   public void cleanup() {
      dispose();
   }

   /** This method is called from within the constructor to
    * initialize the form.
    */
   @SuppressWarnings("unchecked")
   private void initComponents() {

      slicesCheckBox_ = new javax.swing.JCheckBox();
      channelComboBox_ = new javax.swing.JComboBox();
      slicesTextField_ = new javax.swing.JTextField();
      jLabel1 = new javax.swing.JLabel();
      jLabelchannel = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("Pseudo Channels");
      setBounds(new java.awt.Rectangle(300, 300, 150, 150));
      setMinimumSize(new java.awt.Dimension(200, 200));
      setResizable(false);

      slicesCheckBox_.setText("Use Slices");
      slicesCheckBox_.addActionListener(new java.awt.event.ActionListener() {
         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            slicesCheckBox_ActionPerformed(evt);
         }
      });

      channelComboBox_.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4" }));
      channelComboBox_.addActionListener(new java.awt.event.ActionListener() {
         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            channelComboBox_ActionPerformed(evt);
         }
      });

      slicesTextField_.setMinimumSize(new Dimension(100,20));
      slicesTextField_.addActionListener(new java.awt.event.ActionListener() {
         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            slicesComboBox_ActionPerformed(evt);
         }
      });

      jLabel1.setText("Number of Slices");
      jLabelchannel.setText("Number of Channels");

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelchannel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(channelComboBox_, javax.swing.GroupLayout.PREFERRED_SIZE, 0, 153))
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(slicesTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                  .addGap(38, 38, 38))
               .addGroup(layout.createSequentialGroup()
                  .addComponent(slicesCheckBox_)
                  .addContainerGap(121, Short.MAX_VALUE))))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGap(11, 11, 11)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(channelComboBox_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabelchannel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(slicesCheckBox_)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(slicesTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel1))
            .addGap(25, 25, 25))
      );

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

   public void setChannels(int channels) {
      channelComboBox_.setSelectedIndex(channels-1);
   }

   public String getSlices() {
       return slicesTextField_.getText();
   }

   /**
    * Indicates users choice for rotation:
    * 0 - 0 degrees
    * 1 - 90 degrees
    * 2 - 180 degrees
    * 3 - 270 degrees
    * degrees are anti-clockwise
    * 
    * @return coded rotation
    */
   public final int getChannels() {
      if (C2.equals((String) channelComboBox_.getSelectedItem())) {
         return PseudoChannelProcessor.C2;
      }
      if (C3.equals((String) channelComboBox_.getSelectedItem())) {
         return PseudoChannelProcessor.C3;
      }
      if (C4.equals((String)channelComboBox_.getSelectedItem())) {
         return PseudoChannelProcessor.C4;
      }
      return PseudoChannelProcessor.C1;
   }

   public final boolean getUseSlices() {
      return slicesCheckBox_.isSelected();
   }


   
   @Override
   public PropertyMap getSettings() {
      PropertyMap.Builder builder = PropertyMaps.builder(); 
      builder.putInteger("channels", getChannels());
      builder.putString("slices", getSlices());
      builder.putBoolean("useSlices", getUseSlices());
      return builder.build();
   }
}