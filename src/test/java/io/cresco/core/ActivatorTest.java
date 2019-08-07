package io.cresco.core;

import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;

import static org.junit.jupiter.api.Assertions.assertTrue;


class ActivatorTest {


    @Test
    void start() {

        try {
            // get bundle context
            BundleContext bundleContext = MockOsgi.newBundleContext();

            Activator activator = new Activator();
            activator.start(bundleContext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //assertTrue(true);


    }

    @Test
    void stop() {

        int i = 0;
        i += 1;

        assertTrue(true);

    }
}