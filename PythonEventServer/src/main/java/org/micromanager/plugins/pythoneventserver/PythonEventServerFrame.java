
package org.micromanager.plugins.pythoneventserver;

import com.google.common.eventbus.Subscribe;
import mmcorej.CMMCore;
import mmcorej.org.json.JSONArray;
import mmcorej.org.json.JSONException;
import mmcorej.org.json.JSONObject;
import org.micromanager.Studio;
import org.micromanager.acquisition.AcquisitionEndedEvent;
import org.micromanager.acquisition.AcquisitionSequenceStartedEvent;
import org.micromanager.acquisition.AcquisitionStartedEvent;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.DataProviderHasNewImageEvent;
import org.micromanager.display.internal.event.DataViewerAddedEvent;
import org.micromanager.events.*;
import org.micromanager.internal.ConfigGroupPad;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.dialogs.AcqControlDlg;
import org.micromanager.internal.interfaces.AcqSettingsListener;
import org.micromanager.internal.utils.WindowPositioning;
import org.micromanager.internal.zmq.ZMQUtil;
import org.zeromq.SocketType;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import static org.zeromq.ZMQ.*;

/**
 * ZMQ based Server that relays Micro-Manager events to python
 * <p>
 * THis is called and started when PyhtonEventServer Plugin is
 * started from Micro-Manager. It starts a Thread that uses a
 * ZMQ PUB socket to publish events that can be subscribed to from
 * python. This can run in parallel with pycromanager to make use
 * of those events.
 * <p>
 * To get the events, the thread implements AcqSettingsListener and
 * subscribes to the EventBus via the EventManager of Micro-Manager.
 * When an event is received it relays the same event to the custom
 * socket. Exceptions are GUIRefreshEvents, that try to get info
 * from the Configuration settings to see if it was triggered by
 * a change there. It then sends out this information using the
 * CustomSettinsEvent class. This is because there does not seem
 * to be another event triggered by that.
 * The other exception are changes in theMDA window, where the
 * AcqSettingsListener is notified and just sends the full Settings.
 *
 * @author Willi Stepp
 * @version 0.1
 */

public class PythonEventServerFrame extends JFrame {

   private final JCheckBox liveCheckBox_;
   private Studio studio_;
   private ConfigGroupPad pad_;
   private final JTextArea logTextArea;
   private final JScrollPane scrollPane;
   private Socket socket_;
   private ServerThread server;
   private JPanel panel = new JPanel();
   private BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
   public ZMQUtil util_;
   private AcqControlDlg acw_;
   private long lastStageTime;

   public PythonEventServerFrame(Studio studio) {
      super("Python Event Server GUI");
      studio_ = studio;
      acw_ = ((MMStudio) studio_).uiManager().getAcquisitionWindow();
      // Set up the server
      server = new ServerThread();
      server.init();
      server.start();
      System.out.println("Server started");

      // try the same with the mmserver
      CMMCore core = studio_.getCMMCore();
      HashSet<ClassLoader> classLoaders = new HashSet<>();
      classLoaders.add(core.getClass().getClassLoader());
      util_ = new ZMQUtil(classLoaders, new String[]{});


      // Add a function that closes the server when the window is closed
      super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      super.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            exitPlugin();
         }
      });

      // Adjust the window
      JLabel title = new JLabel("Python Event Server");
      title.setFont(new Font("Arial", Font.BOLD, 14));
      title.setMaximumSize( new Dimension(200, 30));
      title.setAlignmentX(CENTER_ALIGNMENT);


      liveCheckBox_ = new javax.swing.JCheckBox();
      liveCheckBox_.setText("Live Mode Events");

      logTextArea = new JTextArea(30,100);
      logTextArea.setAlignmentX(CENTER_ALIGNMENT);

      scrollPane = new JScrollPane( logTextArea );
      DefaultCaret caret = (DefaultCaret)logTextArea.getCaret();
      caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

      panel.setLayout(boxLayout);
      panel.add(title);
      panel.add(scrollPane);
      panel.add(liveCheckBox_);
      panel.setAlignmentX(CENTER_ALIGNMENT);

      super.add(panel);
      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
              getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(100, 100);
      super.setSize(new Dimension(100, 100));
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);
      super.pack();

   }

   public void exitPlugin(){
      server.close();
      super.dispose();
      System.out.println("Closed Server and Frame");
   }


   public class ServerThread extends Thread implements AcqSettingsListener {


      public void init(){
         final Context context = context(1);
         socket_ = context.socket(SocketType.PUB);
//         socket_.bind("tcp://localhost:5556");
         socket_.bind("tcp://*:5556");
         studio_.events().registerForEvents(this);
         MMStudio studio;
         studio = (MMStudio) studio_;

         pad_ =  studio.uiManager().frame().getConfigPad();

         ((MMStudio) studio_).getAcquisitionEngine().addSettingsListener(this);

         lastStageTime = System.currentTimeMillis();
      }

      public void close(){
         socket_.unbind("tcp://*:5556");
         socket_.close();
         studio_.events().unregisterForEvents(this);
         super.interrupt();
      }

      @Subscribe
      public void onConfigGroupChanged(ConfigGroupChangedEvent event) {
         addLog("ConfigGroupChangedEvent " + event.getGroupName());
      }


      @Subscribe
      public void onGUIRefresh(GUIRefreshEvent event){
         // This also fires every time some value in the Table is changed, as we don't get events directly from that,
         // we will construct our own event and send that out again over the EventBus, so that things are consistent.
         String group = pad_.getSelectedGroup();
         // Get what changed from the table
         // Put this information into a List of CustomSettingsEvents for each setting that has been changed
         String state;
         java.util.List<CustomSettingsEvent> changedSettings = new ArrayList<>();
         try {
            state = studio_.core().getConfigGroupState(group).getVerbose();
            // state looks something like this: <html>Dummy_488_Power:Power (% of max)=99.8000<br></html>
            // with different settings that are changed separated by <br>s
            System.out.println(state);
            String[] changeList = state.substring(6, state.length() - 7).split("<br>");
            for (String change : changeList) {
               System.out.println(change);
               String device = change.split(":")[0];
               String setting = change.split(":")[1].split("=")[0];
               String value = change.split(":")[1].split("=")[1];
               changedSettings.add(new CustomSettingsEvent(device, setting, value));
            }
         } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
         }

         if (changedSettings.isEmpty()) {
//            No setting has changed we will send the current exposure time setting
            double exposure = 0;
            try {
               exposure = studio_.core().getExposure();
            } catch (Exception e) {
               e.printStackTrace();
            }
            studio_.events().post(new CustomSettingsEvent("exposure", "time_ms", String.valueOf(exposure)));
         }

         for (CustomSettingsEvent setting : changedSettings){
            studio_.events().post(setting);
         }
         addLog("GUIRefreshEvent");
      }

      // Subscribe to the events that are sent by the special RefreshGUI call above
      @Subscribe
      public void onSettingsChanged(CustomSettingsEvent event){
         sendJSON(event);
         addLog("CustomSettingsEvent " + event.getDevice() + ":"
                 + event.getProperty() + "=" + event.getValue());
      }

      @Override
      public void settingsChanged() {
         System.out.println("Setting changed");

//         PropertyChangeEvent e = new PropertyChangeEvent(1, "test",0,1);
//         acw_.propertyChange(e);

         SequenceSettings sequenceSettings = studio_.acquisitions().getAcquisitionSettings();
         CustomMDAEvent settingsEvent = new CustomMDAEvent(sequenceSettings);
         studio_.events().post(settingsEvent);
      }

      // This subscribes to the Event that is posted when a setting in the MDA window changes
      // These events are posted by the custom settingslistener implemented in a second thread in SettingsListener
      @Subscribe
      public void onCustomMDA(CustomMDAEvent event){
         sendJSON(event);
         addLog("CustomMDAEvent");
      }


      @Subscribe
      public void onExposureChanged(ExposureChangedEvent event) {
         sendJSON(event);
         addLog("ExposureChangedEvent " + event.getNewExposureTime());
      }

      @Subscribe
      public void onStagePositionChanged(StagePositionChangedEvent event){
         long now = System.currentTimeMillis();
         System.out.println(now - lastStageTime);

         // Limit the frequency of these events
         sendJSON(event);
         addLog("StagePositionChangedEvent");
         lastStageTime = now;
      }

      @Subscribe
      public void onXYStagePositionChanged(XYStagePositionChangedEvent event){
         sendJSON(event);
         addLog("XYStagePositionChangedEvent");
      }

      @Subscribe
      public void onAcquisitionEnded(AcquisitionEndedEvent event)  {
         sendJSON(event);
         addLog("AcquisitionEndedEvent");
      }

      @Subscribe
      public void onAcquisitionStarted(AcquisitionStartedEvent event){
         sendJSON(event);
         event.getDatastore().registerForEvents(this);
         addLog("AcquisitionStartedEvent");
      }

      public void onWindowAddedEvent(DataViewerAddedEvent event) {
         sendJSON(event);
         event.getDataViewer().getDataProvider().registerForEvents(this);
         addLog("DataViewerAddedEvent");
      }

      @Subscribe
      public void onAcquisitionStarted(AcquisitionSequenceStartedEvent event) {
         sendJSON(event);
         addLog("AcquisitionSequenceStartedEvent");
      }

      @Subscribe
      public void onNewImage(DataProviderHasNewImageEvent event){
         sendJSON(event);
         addLog("DataProviderHasNewImageEvent");
      }

      @Subscribe
      public void onDataViewerAddedEvent(DataViewerAddedEvent event){
         addLog("DataViewerAddedEvent");
      }

      @Subscribe
      public void onLiveMode(LiveModeEvent event) {
         if (event.isOn() & liveCheckBox_.isSelected()) {
            studio_.getSnapLiveManager().getDisplay().getDataProvider().registerForEvents(this);
         } else {
            studio_.getSnapLiveManager().getDisplay().getDataProvider().unregisterForEvents(this);
         }
         sendJSON(event);
         addLog("LiveModeEvent");
      }


//      @Subscribe
//      public void onImageOverwritten(DefaultImageOverwrittenEvent event) {
//         sendJSON(event);
//         addLog("ImageOverwritten");
//      }


      void addLog(String eventMsg){
         String currentText = logTextArea.getText();
         String newEvent =  new Date() + " " + eventMsg;
         logTextArea.setText(newEvent + "\n" + currentText);
       }

      public void sendJSON(Object event){
         JSONObject json = new JSONObject();
         util_.serialize(event, json, 5556);
         socket_.send("StandardEvent " + json);
      }

   }



}



