package org.micromanager.plugins.pythoneventserver;

import org.micromanager.acquisition.SequenceSettings;

public class CustomMDAEvent {
    private final SequenceSettings newSettings_;

    public CustomMDAEvent(SequenceSettings newSettings) {
        newSettings_ = newSettings;
    }

    public SequenceSettings getSettings() {
        return newSettings_;
    }

}
