package io.cresco.core;

import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;

class ActivatorTest {



    @Test
    void start() {

        try {

            BundleContext bundleContext = MockOsgi.newBundleContext();
            Activator activator = new Activator();
            activator.start(bundleContext);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Test
    void stop() {


        try {

        BundleContext bundleContext = MockOsgi.newBundleContext();
        Activator activator = new Activator();
        activator.start(bundleContext);
        activator.stop(bundleContext);

    } catch (Exception ex) {
        ex.printStackTrace();
    }


}
}