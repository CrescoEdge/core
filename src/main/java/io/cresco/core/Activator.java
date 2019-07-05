package io.cresco.core;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;




/**
 * Bundle Activator.<br/>
 * Looks up the Configuration Admin service and on activation will configure Pax Logging.
 * On deactivation will unconfigure Pax Logging.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.2.2, November 26, 2008
 */
public final class Activator
        implements BundleActivator
{
    private List<String> levelList;


    /**
     * {@inheritDoc}
     * Configures Pax Logging via Configuration Admin.
     */
    public void start( final BundleContext bundleContext )
            throws Exception {


        /*
        installInternalBundleJars(bundleContext,"org.osgi.service.cm-1.6.0.jar").start();
        Bundle loggerService = installInternalBundleJars(bundleContext,"pax-logging-service-1.10.1.jar");
        Bundle loggerAPI = installInternalBundleJars(bundleContext,"pax-logging-api-1.10.1.jar");
        loggerService.start();
        loggerAPI.start();
        */

    }


    /**
     * {@inheritDoc}
     * UnConfigures Pax Logging via Configuration Admin.
     */
    public void stop( final BundleContext bundleContext )
            throws Exception
    {


        System.out.println("--STOPPING CONTROLLER");

        System.out.println("Stopped listening for service events.");

        /*
        ServiceComponentRuntime serviceComponentRuntime = getServiceComponentRuntime(bundleContext);

        ComponentDescriptionDTO agentDTO = serviceComponentRuntime.getComponentDescriptionDTO(bundleContext.getBundle(), "AgentService.class");
        if(agentDTO != null) {
            System.out.println(agentDTO.deactivate);
        } else {
            System.out.println("AGENT NOT FOUND");
        }
        */
        //Returns the ComponentDescriptionDTO declared with the specified name by the specified bundle.

        System.out.println("--STOPPED CONTROLLER");

    }


    private ServiceComponentRuntime getServiceComponentRuntime(BundleContext srcBc) {

        ServiceComponentRuntime serviceComponentRuntime = null;
        try {

            ServiceReference<?>[] servRefs = null;

            while(servRefs == null) {
                servRefs = srcBc.getServiceReferences(ServiceComponentRuntime.class.getName(), null);

                if (servRefs == null || servRefs.length == 0) {

                    System.out.println("ERROR: service runtime not found, this will cause problems with shutdown");
                    Thread.sleep(1000);

                } else {

                    for (ServiceReference sr : servRefs) {

                        System.out.println("BLAH");

                        boolean assign = sr.isAssignableTo(srcBc.getBundle(), ServiceComponentRuntime.class.getName());
                        if (assign) {

                            ServiceReference scrServiceRef = srcBc.getServiceReference(ServiceComponentRuntime.class.getName());
                            serviceComponentRuntime = (ServiceComponentRuntime) srcBc.getService(scrServiceRef);

                        } else {
                            System.out.println("Unable to assign service runtime");
                        }

                    }
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return serviceComponentRuntime;
    }

    private Bundle installInternalBundleJars(BundleContext context, String bundleName) {



        Bundle installedBundle = null;
        try {
            URL bundleURL = getClass().getClassLoader().getResource(bundleName);
            if(bundleURL != null) {

                String bundlePath = bundleURL.getPath();
                installedBundle = context.installBundle(bundlePath,
                        getClass().getClassLoader().getResourceAsStream(bundleName));


            } else {
                System.out.println("core installInternalBundleJars() Bundle = null for " + bundleName);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if(installedBundle == null) {
            System.out.println("core installInternalBundleJars () Failed to load bundle " + bundleName + " exiting!");
            System.exit(0);
        }

        return installedBundle;
    }



}
