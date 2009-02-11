package org.sonatype.nexus.jsecurity.realms;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.realms.privileges.PrivilegePropertyDescriptor;

@Component( role = PrivilegePropertyDescriptor.class, hint = "RepositoryPropertyDescriptor" )
public class RepositoryPropertyDescriptor
    implements PrivilegePropertyDescriptor
{
    public static final String ID = "repositoryOrGroupId";
    
    public String getHelpText()
    {
        return "The Repository associated with this Privilege.";
    }

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Repository";
    }
    
    public String getType()
    {
        return "repoOrGroup";
    }
}
