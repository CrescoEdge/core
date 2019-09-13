package io.cresco.osgi.test;

import java.io.IOException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class MockConfigurationAdminImpl implements ConfigurationAdmin
{

    public Configuration createFactoryConfiguration( String factoryPid ) throws IOException
    {
        return null;
    }


    public Configuration createFactoryConfiguration( String factoryPid, String location ) throws IOException
    {
        return null;
    }


    public Configuration getConfiguration( String pid ) throws IOException
    {
        return null;
    }


    public Configuration getConfiguration( String pid, String location ) throws IOException
    {
        return null;
    }


    public Configuration[] listConfigurations( String filter ) throws IOException, InvalidSyntaxException
    {
        return null;
    }



}