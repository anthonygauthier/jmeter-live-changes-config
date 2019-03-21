package io.github.delirius325.jmeter.config.livechanges;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import java.beans.PropertyDescriptor;

public class LiveChangesBeanInfo extends BeanInfoSupport {
    private static final String PORT = "httpServerPort";
    private static final String CALCULATION_RATE = "calculationRate";

    public LiveChangesBeanInfo() {
        super(LiveChanges.class);

        createPropertyGroup("live_changes", new String[] {
            PORT,
            CALCULATION_RATE
        });

        PropertyDescriptor port = property(PORT);
        port.setValue(DEFAULT, 7566);

        PropertyDescriptor calculationRate = property(CALCULATION_RATE);
        calculationRate.setValue(DEFAULT, 10);
    }
}
