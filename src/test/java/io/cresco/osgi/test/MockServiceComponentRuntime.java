package io.cresco.osgi.test;

import org.osgi.framework.Bundle;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.util.promise.Promise;

import java.util.Collection;

public class MockServiceComponentRuntime implements ServiceComponentRuntime {


    /**
     * @see org.osgi.service.component.runtime.ServiceComponentRuntime#getComponentDescriptionDTOs(org.osgi.framework.Bundle[])
     */
    @Override
    public Collection<ComponentDescriptionDTO> getComponentDescriptionDTOs(Bundle... bundles)
    {
       return null;
    }

    /**
     * @see org.osgi.service.component.runtime.ServiceComponentRuntime#getComponentDescriptionDTO(org.osgi.framework.Bundle, java.lang.String)
     */
    @Override
    public ComponentDescriptionDTO getComponentDescriptionDTO(Bundle bundle, String name)
    {
        return null;
    }

    /**
     * @see org.osgi.service.component.runtime.ServiceComponentRuntime#getComponentConfigurationDTOs(org.osgi.service.component.runtime.dto.ComponentDescriptionDTO)
     */
    @Override
    public Collection<ComponentConfigurationDTO> getComponentConfigurationDTOs(ComponentDescriptionDTO description)
    {
        return null;
    }

    /**
     * @see org.osgi.service.component.runtime.ServiceComponentRuntime#isComponentEnabled(org.osgi.service.component.runtime.dto.ComponentDescriptionDTO)
     */
    @Override
    public boolean isComponentEnabled(ComponentDescriptionDTO description)
    {
        return true;
    }

    /**
     * @see org.osgi.service.component.runtime.ServiceComponentRuntime#enableComponent(org.osgi.service.component.runtime.dto.ComponentDescriptionDTO)
     */
    @Override
    public Promise<Void> enableComponent(ComponentDescriptionDTO description)
    {
        return null;
    }

    /**
     * @see org.osgi.service.component.runtime.ServiceComponentRuntime#disableComponent(org.osgi.service.component.runtime.dto.ComponentDescriptionDTO)
     */
    @Override
    public Promise<Void> disableComponent(ComponentDescriptionDTO description)
    {
       return null;
    }


}
