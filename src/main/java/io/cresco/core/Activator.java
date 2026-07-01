package io.cresco.core;

import io.cresco.library.core.CoreState;
import org.osgi.framework.*;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.util.promise.Promise;
import org.osgi.util.tracker.ServiceTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;


public final class Activator implements BundleActivator {

    private static final long SCR_WAIT_MS = 10000L;
    private static final long DISABLE_WAIT_MS = 10000L;

    private Logger logService;
    private CoreState coreState;


    public void start( final BundleContext bundleContext )  {

        String logIdent = this.getClass().getName().toLowerCase();
        logService = LoggerFactory.getLogger(logIdent);

        coreState = new CoreStateImpl(bundleContext);

        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put("Language", "English");
        bundleContext.registerService(
                CoreState.class.getName(),coreState, props);

    }

    public void stop( final BundleContext bundleContext )  {

        // Track SCR rather than spin in an unbounded loop (which could hang framework shutdown
        // if SCR has already gone away). The tracker is closed in the finally block.
        ServiceTracker<ServiceComponentRuntime, ServiceComponentRuntime> scrTracker =
                new ServiceTracker<>(bundleContext, ServiceComponentRuntime.class, null);
        scrTracker.open();
        try {

            ServiceComponentRuntime serviceComponentRuntime = scrTracker.getService();
            if (serviceComponentRuntime == null) {
                serviceComponentRuntime = scrTracker.waitForService(SCR_WAIT_MS);
            }

            Bundle controllerBundle = null;

            for (Bundle bundle : bundleContext.getBundles()) {

                String bundleName = bundle.getSymbolicName();
                if (bundleName != null) {
                    if (bundleName.equals("io.cresco.controller")) {
                        controllerBundle = bundle;
                    }
                }
            }

            if (controllerBundle != null) {

                if(serviceComponentRuntime != null) {
                    ComponentDescriptionDTO agentDTO = serviceComponentRuntime.getComponentDescriptionDTO(controllerBundle, "io.cresco.agent.core.AgentServiceImpl");
                    if ((agentDTO != null) && (serviceComponentRuntime.isComponentEnabled(agentDTO))) {

                        // Disable the controller's DS component ONCE, then wait (bounded) for that
                        // single disable to complete. (Previously this called disableComponent twice
                        // and waited without a timeout.)
                        Promise<Void> disabled = serviceComponentRuntime.disableComponent(agentDTO);
                        long deadline = System.currentTimeMillis() + DISABLE_WAIT_MS;
                        while (!disabled.isDone() && System.currentTimeMillis() < deadline) {
                            Thread.sleep(100);
                        }

                    }
                } else {
                    logService.error("ERROR: serviceComponentRuntime == null; controller shutdown may be incomplete");
                }
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            logService.error("Logger Out : " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            scrTracker.close();
        }

    }


}
