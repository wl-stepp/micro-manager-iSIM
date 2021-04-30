package org.micromanager.plugins.pythoneventserver;


public class CustomSettingsEvent {
    private final String device_;
    private final String property_;
    private final String value_;

    public CustomSettingsEvent(String device, String property, String value) {
        device_ = device;
        property_ = property;
        value_ = value;
    }

    public String getValue() {
        return value_;
    }

    public String getProperty() {
        return property_;
    }

    public String getDevice() {
        return device_;
    }
}


