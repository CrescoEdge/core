package io.cresco.core;

import io.cresco.library.core.CoreState;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

public class CoreStateImpl implements CoreState {

    private Logger logService;
    private BundleContext bundleContext;

    public CoreStateImpl(BundleContext bundleContext) {
        String logIdent = this.getClass().getName().toLowerCase();
        logService = LoggerFactory.getLogger(logIdent);
        this.bundleContext = bundleContext;

    }


    @Override
    public boolean updateController(String jarPath) {
        boolean isRestarted = false;
        try {

            Runnable r = new Runnable() {
                public void run() {
                    try {

                        File tempFile = new File(jarPath);
                        boolean exists = tempFile.exists();
                        if(exists) {

                            boolean isEmbedded = true;
                            String existingControllerPath = null;

                            Bundle controllerBundle = getController();

                            String jarLocation = controllerBundle.getLocation();
                            if(jarLocation.contains("!/")) {
                                String[] jarLocations = jarLocation.split("!/");
                                existingControllerPath = jarLocations[1];
                            } else {
                                isEmbedded = false;
                                existingControllerPath = jarLocation;
                            }

                            //stop controller
                            stopController();

                            //uninstall controller
                            controllerBundle.uninstall();


                            try {
                                controllerBundle = installExternalBundleJars(jarPath);
                                controllerBundle.start();

                            } catch (Exception ex) {
                                ex.printStackTrace();

                                if(isEmbedded) {
                                    controllerBundle = installInternalBundleJars(existingControllerPath);
                                } else {
                                    controllerBundle = installExternalBundleJars(existingControllerPath);
                                }
                                controllerBundle.start();
                            }

                        } else {
                            System.out.println("controller jarfile: " + jarPath + " not found!");
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
    public boolean restartController() {
        boolean isRestarted = false;
        try {

            Runnable r = new Runnable() {
                public void run() {
                    try {

                        stopController();
                        startController();

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


    public Bundle getController()  {
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

    private boolean startController() {

        boolean isRestarted = false;
        try {

            boolean hasLaunched = false;

            while (!hasLaunched) {
                Bundle controllerBundle = getController();
                if (controllerBundle != null) {
                    System.out.println("Controller Bundle found on Start");
                    System.out.println("Starting Controller");
                    controllerBundle.start();
                    //32 is started
                    while (controllerBundle.getState() != 32) {
                        System.out.println("Waiting for controller to start : state=" + controllerBundle.getState());

                        Thread.sleep(1000);
                    }
                    System.out.println("Controller Started");
                    hasLaunched = true;

                } else {
                    System.out.println("Controller Bundle not found on Start!");
                }
                Thread.sleep(1000);

            }
            isRestarted = true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isRestarted;
    }

    private boolean stopController() {

        boolean isRestarted = false;
        try {
            boolean hasLaunched = true;

            while (hasLaunched) {
                Bundle controllerBundle = getController();
                if (controllerBundle != null) {
                    System.out.println("Controller Bundle found on Stop");
                    if(controllerBundle.getState() == 32) {
                        System.out.println("Stopping Controller");
                        controllerBundle.stop();
                        while ((controllerBundle.getState() != 26) && (controllerBundle.getState() != 4)) {
                            System.out.println("Waiting for controller to start : state=" + controllerBundle.getState());
                            Thread.sleep(1000);
                        }
                        System.out.println("Controller Stopped");

                    } else {
                        System.out.println("Controller Bundle unexpected state: " + controllerBundle.getState());
                    }

                    hasLaunched = false;

                } else {
                    System.out.println("Controller Bundle not found on Stop!");
                }
                Thread.sleep(1000);

            }

            isRestarted = true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isRestarted;
    }


    private Bundle installInternalBundleJars(String bundleName) {

        Bundle installedBundle = null;
        try {
            URL bundleURL = getClass().getClassLoader().getResource(bundleName);
            if(bundleURL != null) {

                String bundlePath = bundleURL.getPath();
                installedBundle = bundleContext.installBundle(bundlePath,
                        getClass().getClassLoader().getResourceAsStream(bundleName));


            } else {
                System.out.println("Bundle = null for " + bundleName);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if(installedBundle == null) {
            System.out.println("installInternalBundleJars() + Failed to load bundle " +bundleName + " exiting!");

            System.exit(0);
        }

        return installedBundle;
    }

    private Bundle installExternalBundleJars(String bundleName) {

        Bundle installedBundle = null;
        try {
            //URL bundleURL = new URL("file://" + bundleName);
            //if(bundleURL != null) {

            installedBundle = bundleContext.installBundle("file://" + bundleName);


            //} else {
            //    System.out.println("Bundle = null for " + bundleName);
            //}
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if(installedBundle == null) {
            System.out.println("installInternalBundleJars() + Failed to load bundle " +bundleName + " exiting!");
        }

        return installedBundle;
    }



}


