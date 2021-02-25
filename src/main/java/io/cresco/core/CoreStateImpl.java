package io.cresco.core;

import io.cresco.library.core.CoreState;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreStateImpl implements CoreState {

    private Logger logService;


    public CoreStateImpl(BundleContext bundleContext) {
        String logIdent = this.getClass().getName().toLowerCase();
        logService = LoggerFactory.getLogger(logIdent);

    }

    public boolean restartController() {
        boolean isRestarted = false;
        try {

            isRestarted = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isRestarted;
    }


    public boolean stopController( final BundleContext bundleContext)  {
        boolean isStopped = false;
        try {

            ServiceComponentRuntime serviceComponentRuntime = getServiceComponentRuntime(bundleContext);

            Bundle controllerBundle = getController(bundleContext);

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
            ex.printStackTrace();
        }
        return isStopped;
    }

    public Bundle getController( final BundleContext bundleContext)  {
        Bundle controllerBundle = null;
        try {

            for (Bundle bundle : bundleContext.getBundles()) {

                String bundleName = bundle.getSymbolicName();
                if (bundleName != null) {
                    if (bundleName.equals("io.cresco.controller")) {
                        controllerBundle = bundle;
                    }
                }
            }

        } catch (Exception ex) {
            logService.error("Logger Out : " + ex.getMessage());
            ex.printStackTrace();
        }
        return controllerBundle;
    }


    public ServiceComponentRuntime getServiceComponentRuntime(BundleContext srcBc) {


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


