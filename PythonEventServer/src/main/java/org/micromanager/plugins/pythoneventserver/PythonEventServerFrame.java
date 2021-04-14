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
package org.micromanager.plugins.pythoneventserver;

import com.google.common.eventbus.Subscribe;

import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.swing.*;

//Server for the events
import static org.zeromq.ZMQ.*;

import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.AcquisitionWrapperEngine;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.dialogs.AcqControlDlg;
import org.micromanager.data.DataProvider;
import org.micromanager.data.DataProviderHasNewImageEvent;
import org.micromanager.data.Datastore;
import org.micromanager.display.DisplayWindow;
import org.zeromq.SocketType;

import org.micromanager.events.*;

import org.micromanager.Studio;
import org.micromanager.internal.utils.WindowPositioning;





// Imports for MMStudio internal packages
// Plugins should not access internal packages, to ensure modularity and
// maintainability. However, this plugin code is older than the current
// MMStudio API, so it still uses internal classes and interfaces. New code
// should not imitate this practice.


public class PythonEventServerFrame extends JFrame {

   private Studio studio_;
   private final JTextArea exposureTimeLabel_;
   private Socket socket_;
   private ServerThread server;
   private JPanel panel = new JPanel();
   private BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);

   public PythonEventServerFrame(Studio studio) {
      super("Python Event Server GUI");
      studio_ = studio;

      server = new ServerThread();
      server.init();
      server.start();

      JLabel title = new JLabel("Python Event Server");
      title.setFont(new Font("Arial", Font.BOLD, 14));
      title.setMaximumSize( new java.awt.Dimension(200, 30));


      exposureTimeLabel_ = new JTextArea(30,100);

      panel.setLayout(boxLayout);

      title.setAlignmentX(CENTER_ALIGNMENT);
      exposureTimeLabel_.setAlignmentX(CENTER_ALIGNMENT);

      panel.add(title);
      panel.add(exposureTimeLabel_);

      panel.setAlignmentX(CENTER_ALIGNMENT);
      super.add(panel);
      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
              getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(100, 100);
      super.setSize(new java.awt.Dimension(100, 100));
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);
      super.pack();

   }

   public class ServerThread extends Thread {

      public void init(){
         final Context context = context(1);
         socket_ = context.socket(SocketType.REP);
         socket_.bind("tcp://localhost:5556");
         studio_.events().registerForEvents(this);
      }


      @Subscribe
      public void onExposureChanged(ExposureChangedEvent event) {
               byte[] reply = socket_.recv(0);
               String response = "ExposureChangedEvent";
               socket_.send(response.getBytes(CHARSET), 0);
               addLog(response);
      }

      @Subscribe
      public void onNewImage(DataProviderHasNewImageEvent event) {
         byte[] reply = socket_.recv(0);
         String response = "DataProviderHasNewImageEvent";
         socket_.send(response.getBytes(CHARSET), 0);
         addLog(response);
      }
      

      @Subscribe
      public void onAcquisitionStarted(AcquisitionSequenceStartedEvent event) {

         byte[] reply = socket_.recv(0);
         String response = "AcquisitionStartedEvent";
         socket_.send(response.getBytes(CHARSET), 0);
         addLog(response);
      }

      void addLog(String eventMsg){
         String currentText = exposureTimeLabel_.getText();
         String newEvent =  new Date() + " " + eventMsg;
         exposureTimeLabel_.setText(newEvent + "\n" + currentText);

      }
   }
}



