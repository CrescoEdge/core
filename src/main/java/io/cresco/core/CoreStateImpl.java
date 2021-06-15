package io.cresco.core;

import com.sun.corba.se.spi.servicecontext.UEInfoServiceContext;
import io.cresco.library.core.CoreState;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CoreStateImpl implements CoreState {

    private Logger logService;
    private BundleContext bundleContext;

    public CoreStateImpl(BundleContext bundleContext) {



        String logIdent = "io.cresco.agent.core" + "." + this.getClass().getName().toLowerCase();
        logService = LoggerFactory.getLogger(logIdent);
        this.bundleContext = bundleContext;

    }

    public boolean updateController(String jarPathString) {
        boolean isRestarted = false;

        try {
            Path jarPath = Paths.get(jarPathString);
            if(jarPath.toFile().exists()) {

                Runnable r = new Runnable() {
                    public void run() {
                        try {


                            Bundle originalControllerBundle = getController();

                            logService.debug("Stopping Controller");
                            //stop controller
                            stopController();

                            logService.debug("Uninstalling old Controller");
                            //uninstall controller
                            originalControllerBundle.uninstall();

                            try {
                                Bundle newControllerBundle = installBundleJars(jarPath.toUri().toURL().toString());
                                //Bundle newControllerBundle = null;
                                newControllerBundle.start();
                                logService.debug("Starting new controller");

                            } catch (Exception ex) {
                                logService.error("Failed to install new controller");
                                logService.error("recovery not yet implemented, self destruct");
                                System.exit(1);
                            }

                        } catch (Exception ex) {
                            logService.error("failure during controller update");
                            ex.printStackTrace();
                        }
                    }
                };

                new Thread(r).start();
                isRestarted = true;
            } else {
                logService.error("jarPath : " + jarPath.toAbsolutePath().toString() + " does not exist!");
            }

        } catch (Exception ex) {
            logService.error("updateController() failure");
            ex.printStackTrace();
        }
        return isRestarted;
    }

    @Override
    public boolean stopController() {
        boolean isStopped = false;
        try {

            Runnable r = new Runnable() {
                public void run() {
                    try {

                        stopControllerInternal();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };

            new Thread(r).start();

            isStopped = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isStopped;
    }

    @Override
    public boolean restartController() {
        boolean isRestarted = false;
        try {

            Runnable r = new Runnable() {
                public void run() {
                    try {

                        stopControllerInternal();
                        startControllerInternal();

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

    @Override
    public boolean killJVM() {
        //only use this when you have something restarting the entire agent, like a service
        boolean isKilled = false;
        try {

            Runnable r = new Runnable() {
                public void run() {
                    try {

                        stopControllerInternal();
                        System.exit(0);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };

            new Thread(r).start();

            isKilled = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isKilled;
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

    private boolean startControllerInternal() {

        boolean isRestarted = false;
        try {

            boolean hasLaunched = false;

            while (!hasLaunched) {
                Bundle controllerBundle = getController();
                if (controllerBundle != null) {
                    logService.error("Controller Bundle found on Start");
                    logService.error("Starting Controller");
                    controllerBundle.start();
                    //32 is started
                    while (controllerBundle.getState() != 32) {
                        logService.error("Waiting for controller to start : state=" + controllerBundle.getState());

                        Thread.sleep(1000);
                    }
                    logService.error("Controller Started");
                    hasLaunched = true;

                } else {
                    logService.error("Controller Bundle not found on Start!");
                }
                Thread.sleep(1000);

            }
            isRestarted = true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isRestarted;
    }

    private boolean stopControllerInternal() {

        boolean isRestarted = false;
        try {
            boolean hasLaunched = true;

            while (hasLaunched) {
                Bundle controllerBundle = getController();
                if (controllerBundle != null) {
                    logService.error("Controller Bundle found on Stop : State = " + controllerBundle.getState());
                    if(controllerBundle.getState() == 32) {
                        logService.error("Stopping Controller");
                        controllerBundle.stop();
                        while ((controllerBundle.getState() != 26) && (controllerBundle.getState() != 4)) {
                            logService.error("Waiting for controller to start : state=" + controllerBundle.getState());
                            Thread.sleep(1000);
                        }
                        logService.error("Controller Stopped");

                    } else {
                        logService.error("Controller Bundle unexpected state: " + controllerBundle.getState());
                    }

                    hasLaunched = false;

                } else {
                    logService.error("Controller Bundle not found on Stop!");
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



    private Bundle installBundleJars(String bundlePath) {

        Bundle installedBundle = null;
        try {
            if (bundlePath.contains("!/")) {

                String[] jarLocations = bundlePath.split("!/");
                String bundleName = jarLocations[1];

                URL bundleURL = getClass().getClassLoader().getResource(bundleName);

                    bundlePath = bundleURL.getPath();
                    installedBundle = bundleContext.installBundle(bundlePath,
                            getClass().getClassLoader().getResourceAsStream(bundleName));


            } else {
                installedBundle = bundleContext.installBundle(bundlePath);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if(installedBundle == null) {
            logService.error("installBundleJars() + Failed to load bundle " + bundlePath + " exiting!");
        }

        return installedBundle;
    }


}


