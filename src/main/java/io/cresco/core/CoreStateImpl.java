package io.cresco.core;

import io.cresco.library.core.CoreState;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreStateImpl implements CoreState {

    private Logger logService;
    private BundleContext bundleContext;

    public CoreStateImpl(BundleContext bundleContext) {
        String logIdent = this.getClass().getName().toLowerCase();
        logService = LoggerFactory.getLogger(logIdent);
        this.bundleContext = bundleContext;

    }

    @Override
    public boolean restartController() {
        boolean isRestarted = false;
        try {

            Runnable r = new Runnable() {
                public void run() {
                    try {

                        boolean hasLaunched = false;
                        while (hasLaunched) {
                            Bundle controllerBundle = getController(bundleContext);
                            if (controllerBundle != null) {

                                if(controllerBundle.getState() == 32) {
                                    controllerBundle.stop();
                                    while (controllerBundle.getState() != 26) {
                                        Thread.sleep(1000);
                                    }
                                }

                                hasLaunched = false;

                            }
                            Thread.sleep(1000);

                        }

                        while (!hasLaunched) {
                            Bundle controllerBundle = getController(bundleContext);
                            if (controllerBundle != null) {
                                controllerBundle.start();
                                //32 is started
                                while (controllerBundle.getState() != 32) {
                                    Thread.sleep(1000);
                                }
                                hasLaunched = true;

                            }
                            Thread.sleep(1000);

                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };

            new Thread(r).start();

            isRestarted = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isRestarted;
    }

    @Override
    public boolean restartFramework() {

        try {

            Bundle systemBundle = bundleContext.getBundle(0);
            Thread t = new Thread("Stopper") {
                public void run() {

                    // stopping bundle 0 (system bundle) stops the framework
                    try {

                        systemBundle.update();

                        //systemBundle.stop();

                    } catch (BundleException be) {
                        be.printStackTrace();

                    }
                }
            };
            t.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }


    public boolean startController() {

        Runnable r = new Runnable() {
            public void run() {
                try {
                    boolean hasLaunched = false;
                    while (!hasLaunched) {
                        Bundle controllerBundle = getController(bundleContext);
                        if (controllerBundle != null) {
                                controllerBundle.start();
                                //32 is started
                                while (controllerBundle.getState() != 32) {
                                    Thread.sleep(1000);
                                }
                                hasLaunched = true;

                        }
                        Thread.sleep(1000);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        new Thread(r).start();

        return true;
    }

    public boolean stopController() {


        boolean isStopped = false;

        Runnable r = new Runnable() {
            public void run() {
                try {
                    boolean hasLaunched = false;
                    while (hasLaunched) {
                        Bundle controllerBundle = getController(bundleContext);
                        if (controllerBundle != null) {

                            if(controllerBundle.getState() == 32) {
                                controllerBundle.stop();
                                while (controllerBundle.getState() != 26) {
                                    Thread.sleep(1000);
                                }
                            }

                            hasLaunched = false;

                        }
                        Thread.sleep(1000);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        new Thread(r).start();



        isStopped = true;
        return  isStopped;
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


