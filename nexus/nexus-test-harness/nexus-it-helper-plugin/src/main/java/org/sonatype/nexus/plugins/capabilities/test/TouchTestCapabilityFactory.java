package org.sonatype.nexus.plugins.capabilities.test;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

@Component( role = CapabilityFactory.class, hint = TouchTestCapability.ID )
public class TouchTestCapabilityFactory
    implements CapabilityFactory
{

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    public Capability create( String id )
    {
        return new TouchTestCapability( id, repositoryRegistry );
    }

}
