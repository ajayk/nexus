package org.sonatype.nexus.rest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.Subject;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.maven1.M1LayoutedM2ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.proxy.target.TargetRegistry;
import org.sonatype.nexus.security.WebSecurityUtil;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;

public class ProtectedRepositoryRegistryTest
    extends AbstractNexusTestCase
{

    private RepositoryRegistry repositoryRegistry = null;

    private SecuritySystem securitySystem = null;
    
    private Nexus nexus;

    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );
        ctx.put( "security-xml-file", new File( CONF_HOME, "security.xml" ).getAbsolutePath() );

        // copy the security-configuration
        String resource = this.getClass().getName().replaceAll( "\\.", "\\/" ) + "-security-configuration.xml";
        URL url = Thread.currentThread().getContextClassLoader().getResource( resource );
        try
        {
            FileUtils.copyURLToFile( url, new File( CONF_HOME, "security-configuration.xml" ) );
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( e );
        }
    }
    
    @Override
    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // this will trigger config load!
        this.nexus = lookup( Nexus.class );

        this.repositoryRegistry = this.lookup( RepositoryRegistry.class, "protected" );
        
        this.buildRepository( "repo1" );
        this.buildRepository( "repo2" );

        this.buildGroupRepository( "group1" );
        this.buildGroupRepository( "group2" );

        this.buildShadowRepository( "repo3" );
        this.buildShadowRepository( "repo4" );

        // create a target
        TargetRegistry targetRegistry = this.lookup( TargetRegistry.class );
        Target t1 = new Target( "maven2-all", "All (Maven2)", new Maven2ContentClass(), Arrays
            .asList( new String[] { ".*" } ) );
        targetRegistry.addRepositoryTarget( t1 );
        nexus.getNexusConfiguration().saveConfiguration();

        // setup security
        this.securitySystem = this.lookup( SecuritySystem.class );
        //this.securitySystem.setSecurityEnabled( true );

        // copy the security-configuration
        //String resource = this.getClass().getName().replaceAll( "\\.", "\\/" ) + "-security-configuration.xml";
        //URL url = Thread.currentThread().getContextClassLoader().getResource( resource );
        //FileUtils.copyURLToFile( url, new File( CONF_HOME, "security-configuration.xml" ) );

        // this.securitySystem.start();

    }

    public void testRegistryWithViewAccess()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1user" );

        List<Repository> repositories = this.repositoryRegistry.getRepositories();

        // // this user only has access to repo1, that is all they should see
        Assert.assertEquals( "User should only have access to 'repo1'", 1, repositories.size() );
        Assert.assertEquals( "repo1", repositories.get( 0 ).getId() );

        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }

    public void testRegistryWithViewAccessFacet()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1user" );

        List<Repository> repositories = this.repositoryRegistry.getRepositoriesWithFacet( Repository.class );

        // // this user only has access to repo1, that is all they should see
        Assert.assertEquals( "User should only have access to 'repo1'", 1, repositories.size() );
        Assert.assertEquals( "repo1", repositories.get( 0 ).getId() );

        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }

    public void testRegistryWithViewAccessById()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1user" );

        Repository repository = this.repositoryRegistry.getRepository( "repo1" );

        Assert.assertNotNull( repository );
        Assert.assertEquals( "repo1", repository.getId() );

        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }

    public void testRegistryWithViewAccessByIdNoAccess()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1userNoView" );

        try
        {
            this.repositoryRegistry.getRepository( "repo1" );
            Assert.fail( "expected NoSuchRepositoryAccessException" );
        }
        catch ( NoSuchRepositoryAccessException e )
        {
            // expected
        }
        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }

    public void testRegistryWithViewAccessFacetById()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1user" );

        Repository repository = this.repositoryRegistry.getRepositoryWithFacet( "repo1", Repository.class );

        Assert.assertNotNull( repository );
        Assert.assertEquals( "repo1", repository.getId() );

        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }

    public void testRegistryWithViewAccessFacetByIdNoAccess()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1userNoView" );

        try
        {
            this.repositoryRegistry.getRepositoryWithFacet( "repo1", Repository.class );
            Assert.fail( "expected NoSuchRepositoryAccessException" );
        }
        catch ( NoSuchRepositoryAccessException e )
        {
            // expected
        }
        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }

    public void testRemoveWithAccess()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1user" );

        this.repositoryRegistry.removeRepository( "repo1" );
        try
        {
            this.repositoryRegistry.getRepository( "repo1" );
            Assert.fail( "expected NoSuchRepositoryException" );
        }
        catch ( NoSuchRepositoryException e )
        {
            // expected
        }
        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }

    public void testRemoveWithoutAccess()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1userNoView" );

        try
        {
            this.repositoryRegistry.removeRepository( "repo1" );
            Assert.fail( "expected NoSuchRepositoryAccessException" );
        }
        catch ( NoSuchRepositoryAccessException e )
        {
            // expected
        }
        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }

    public void testRemoveSilentlyWithAccess()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1user" );

        this.repositoryRegistry.removeRepositorySilently( "repo1" );
        try
        {
            this.repositoryRegistry.getRepository( "repo1" );
            Assert.fail( "expected NoSuchRepositoryException" );
        }
        catch ( NoSuchRepositoryException e )
        {
            // expected
        }
        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }

    public void testRemoveSilentlyWithoutAccess()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1userNoView" );

        try
        {
            this.repositoryRegistry.removeRepositorySilently( "repo1" );
            Assert.fail( "expected NoSuchRepositoryAccessException" );
        }
        catch ( NoSuchRepositoryAccessException e )
        {
            // expected
        }
        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }
    
    public void testGetGroupsWithAccess()
    throws Exception
    {
        Subject subject = this.loginUser( "repoall" );
        
        Collection<GroupRepository> groups = this.repositoryRegistry.getRepositoriesWithFacet( GroupRepository.class );
        
//        list should have 2 group repos
        List<String> groupIds = new ArrayList<String>();
        
        for ( GroupRepository groupRepo : groups )
        {
            groupIds.add( groupRepo.getId() );
        }
        
        Assert.assertTrue( groupIds.contains( "group1" ) );
        Assert.assertTrue( groupIds.contains( "group2" ) );
        Assert.assertTrue( groupIds.contains( "public" ) );
        Assert.assertTrue( groupIds.contains( "public-snapshots" ) );
        Assert.assertEquals( 4, groups.size() );
        
        
        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
        
    }

    private Repository buildRepository( String repoId )
        throws Exception
    {
        CRepository repoConfig = new DefaultCRepository();
        repoConfig.setProviderRole( Repository.class.getName() );
        repoConfig.setProviderHint( "maven2" );
        repoConfig.setId( repoId );

        return nexus.createRepository( repoConfig );
    }

    private Repository buildGroupRepository( String repoId )
        throws Exception
    {
        CRepository repoConfig = new DefaultCRepository();
        repoConfig.setProviderRole( GroupRepository.class.getName() );
        repoConfig.setProviderHint( "maven2" );
        repoConfig.setId( repoId );
        
        return nexus.createRepository( repoConfig );
    }

    private Repository buildShadowRepository( String repoId )
        throws Exception
    {
        CRepository repoConfig = new DefaultCRepository();
        repoConfig.setProviderRole( Repository.class.getName() );
        repoConfig.setProviderHint( "maven2" );
        repoConfig.setId( repoId );
        
        nexus.createRepository( repoConfig );

        // now for the shadow
        CRepository shadowConfig = new DefaultCRepository();
        shadowConfig.setProviderRole( ShadowRepository.class.getName() );
        shadowConfig.setProviderHint( "m2-m1-shadow" );
        shadowConfig.setId( repoId + "-shadow" );

        Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
        shadowConfig.setExternalConfiguration( exRepo );
        M1LayoutedM2ShadowRepositoryConfiguration exRepoConf = new M1LayoutedM2ShadowRepositoryConfiguration( exRepo );
        exRepoConf.setMasterRepositoryId( repoConfig.getId() );
        
        return nexus.createRepository( shadowConfig );
    }

    private Subject loginUser( String username )
        throws AuthenticationException
    {
        WebSecurityUtil.setupWebContext( username );
        return this.securitySystem.login( new UsernamePasswordToken( username, "" ) );
    }

}
