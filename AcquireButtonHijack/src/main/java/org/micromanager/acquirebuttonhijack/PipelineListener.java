package org.micromanager.acquirebuttonhijack;

import com.google.common.eventbus.Subscribe;
import org.micromanager.PluginManager;
import org.micromanager.Studio;
import org.micromanager.data.DataManager;
import org.micromanager.data.NewPipelineEvent;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.internal.interfaces.AcqSettingsListener;


public class PipelineListener extends Thread implements AcqSettingsListener {

    private Studio studio_;
    private DataManager dataManager;
    private PluginManager pluginManager_;
    private ProcessorConfigurator pseudoChannels;
    private AcquireButtonHijackFrame acq_hijack_;

    public PipelineListener(Studio studio, AcquireButtonHijackFrame acq_hijack){
        studio_ = studio;
        dataManager = studio_.data();
        pluginManager_ = studio_.plugins();
        acq_hijack_ = acq_hijack;
    }

    public void init(){
        studio_.events().registerForEvents(this);
    }

    @Subscribe
    public void onPipelineChanged(NewPipelineEvent event) {
        System.out.println("Pipeline Changed!");
        // Configure the PseudoChannel Processor
        boolean pseudoChannelsSet = false;
        for (int i=0; i<dataManager.getApplicationPipelineConfigurators(true).size(); i++){
            ProcessorConfigurator plugin = dataManager.getApplicationPipelineConfigurators(true).get(i);
            String pluginName = plugin.getClass().getName();
            String[] names = pluginName.split("\\.");
            if (names[names.length - 1].equals("PseudoChannelConfigurator")){
                acq_hijack_.setPseudoChannels(plugin);
                pseudoChannelsSet = true;
                System.out.println("PseudoChannels Found");
                break;
            }
        }

        if (!pseudoChannelsSet) {
            dataManager.addAndConfigureProcessor(pluginManager_.getProcessorPlugins()
                    .get("org.micromanager.pseudochannels.PseudoChannelPlugin"));
            acq_hijack_.setPseudoChannels(dataManager.getApplicationPipelineConfigurators(true)
                    .get(dataManager.getApplicationPipelineConfigurators(true).size() - 1));
        }
    }

    @Override
    public void settingsChanged() {
    }
}