package io.github.delirius325.jmeter.config.livechanges;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

public class LiveChangesBeanInfo extends BeanInfoSupport {
    private static final String PORT = "httpServerPort";

    public LiveChangesBeanInfo() {
        super(LiveChanges.class);

        createPropertyGroup("livechanges", new String[] {
            PORT
        });

        PropertyDescriptor p = property(PORT);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, 7566);
    }
}
