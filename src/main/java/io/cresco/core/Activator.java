package io.cresco.core;

import org.osgi.framework.*;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class Activator implements BundleActivator {

    private Logger logService;

    public void start( final BundleContext bundleContext ) throws Exception {

        String logIdent = this.getClass().getName().toLowerCase();
        logService = LoggerFactory.getLogger(logIdent);

    }

    public void stop( final BundleContext bundleContext ) throws Exception {


        try {

            ServiceComponentRuntime serviceComponentRuntime = getServiceComponentRuntime(bundleContext);

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

                        serviceComponentRuntime.disableComponent(agentDTO);

                        while (!serviceComponentRuntime.disableComponent(agentDTO).isDone()) {
                            Thread.sleep(100);
                            //System.out.println("Shutdown didn't talk");
                        }

                    } else {
                        logService.error("AGENT NOT FOUND OR NOT ENABLED!");
                        //System.out.println("ERROR: AGENT NOT FOUND OR NOT ENABLED!");
                    }
                } else {
                    logService.error("ERROR: serviceComponentRuntime == null");
                }
            }

        } catch (Exception ex) {
            logService.error("Logger Out : " + ex.getMessage());
            //ex.printStackTrace();
        }

    }

    private ServiceComponentRuntime getServiceComponentRuntime(BundleContext srcBc) {


        ServiceComponentRuntime serviceComponentRuntime = null;

        if(srcBc.getProperty(Constants.FRAMEWORK_VENDOR) != null) {

            try {

                ServiceReference<?>[] servRefs = null;

                while (servRefs == null) {
                    servRefs = srcBc.getServiceReferences(ServiceComponentRuntime.class.getName(), null);

                    if (servRefs == null || servRefs.length == 0) {

                        logService.error("ERROR: service runtime not found, this will cause problems with shutdown");
                        Thread.sleep(1000);

                    } else {

                        for (ServiceReference sr : servRefs) {

                            boolean assign = sr.isAssignableTo(srcBc.getBundle(), ServiceComponentRuntime.class.getName());
                            if (assign) {

                                ServiceReference scrServiceRef = srcBc.getServiceReference(ServiceComponentRuntime.class.getName());
                                serviceComponentRuntime = (ServiceComponentRuntime) srcBc.getService(scrServiceRef);

                            } else {
                                logService.error("Unable to assign service runtime");
                            }

                        }
                    }
                }


            } catch (Exception ex) {
                logService.error("Logger Out : " + ex.getMessage());
                //ex.printStackTrace();
            }
        }

        return serviceComponentRuntime;
    }

    /*
    private Bundle installInternalBundleJars(BundleContext context, String bundleName) {

        Bundle installedBundle = null;
        try {
            URL bundleURL = getClass().getClassLoader().getResource(bundleName);
            if(bundleURL != null) {

                String bundlePath = bundleURL.getPath();
                installedBundle = context.installBundle(bundlePath,
                        getClass().getClassLoader().getResourceAsStream(bundleName));


            } else {
                logService.error("core installInternalBundleJars() Bundle = null for " + bundleName);
            }
        } catch(Exception ex) {
            logService.error("Logger Out : " + ex.getMessage());
            //ex.printStackTrace();
        }

        if(installedBundle == null) {
            logService.error("core installInternalBundleJars () Failed to load bundle " + bundleName + " exiting!");
            System.exit(0);
        }

        return installedBundle;
    }
    */

}
