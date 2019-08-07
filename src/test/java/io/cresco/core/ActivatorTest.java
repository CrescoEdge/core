package io.cresco.core;

import io.cresco.osgi.test.MockBundleContext;
import org.junit.jupiter.api.Test;

class ActivatorTest {



    @Test
    void start() {

        try {

            MockBundleContext bundleContext = new MockBundleContext();
            Activator activator = new Activator();
            activator.start(bundleContext);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Test
    void stop() {


        try {

        MockBundleContext bundleContext = new MockBundleContext();
        Activator activator = new Activator();
        activator.start(bundleContext);
        activator.stop(bundleContext);

    } catch (Exception ex) {
        ex.printStackTrace();
    }


}
}