/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.support.activation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import org.junit.Test;

/**
 * {@link Conditions} UTs.
 *
 * @since 1.10.0
 */
public class ConditionsTest
{

    /**
     * Passed in factories are returned.
     */
    @Test
    public void and01()
    {
        final LogicalConditions logicalConditions = mock( LogicalConditions.class );
        final CapabilityConditions capabilityConditions = mock( CapabilityConditions.class );
        final RepositoryConditions repositoryConditions = mock( RepositoryConditions.class );
        final NexusConditions nexusConditions = mock( NexusConditions.class );
        final Conditions underTest = new Conditions(
            logicalConditions, capabilityConditions, repositoryConditions, nexusConditions
        );
        assertThat( underTest.logical(), is( equalTo( logicalConditions ) ) );
        assertThat( underTest.capabilities(), is( equalTo( capabilityConditions ) ) );
        assertThat( underTest.repository(), is( equalTo( repositoryConditions ) ) );
        assertThat( underTest.nexus(), is( equalTo( nexusConditions ) ) );
    }

}
