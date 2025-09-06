package io.cresco.core;

import io.cresco.library.core.CoreState;
import org.osgi.framework.*;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;


public final class Activator implements BundleActivator {

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

        try {

            ServiceComponentRuntime serviceComponentRuntime = getServiceComponentRuntime(bundleContext);

            Bundle controllerBundle = null;

            for (Bundle bundle : bundleContext.getBundles()) {

                String bundleName = bundle.getSymbolicName();
                if (bundleName != null) {
                    if (bundleName.equals("io.cresco.controller")) {
                        controllerBundle = bundle;
                        //controllerBundle.stop();
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
                            //logService.error("Shutdown didn't talk");
                        }

                    } else {

                        //logService.error("AGENT NOT FOUND OR NOT ENABLED!");
                        //logService.error("ERROR: AGENT NOT FOUND OR NOT ENABLED!");
                    }
                } else {
                    logService.error("ERROR: serviceComponentRuntime == null");
                }
            }

        } catch (Exception ex) {
            logService.error("Logger Out : " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    private ServiceComponentRuntime getServiceComponentRuntime(BundleContext srcBc) {


        ServiceComponentRuntime serviceComponentRuntime = null;

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
                                if(srcBc.getService(scrServiceRef) instanceof ServiceComponentRuntime) {
                                    serviceComponentRuntime = (ServiceComponentRuntime) srcBc.getService(scrServiceRef);
                                } else {
                                    logService.error("Reference not instance of " + ServiceComponentRuntime.class.getName());
                                }

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

        return serviceComponentRuntime;
    }


}
