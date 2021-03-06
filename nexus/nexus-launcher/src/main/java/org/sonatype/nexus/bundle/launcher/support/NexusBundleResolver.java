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
package org.sonatype.nexus.bundle.launcher.support;

import org.sonatype.inject.Nullable;
import org.sonatype.sisu.bl.support.resolver.MavenBridgedBundleResolver;
import org.sonatype.sisu.maven.bridge.MavenArtifactResolver;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Default Nexus bundle configuration.
 *
 * @since 1.10.0
 */
@Named
@NexusSpecific
public class NexusBundleResolver
    extends MavenBridgedBundleResolver
{

    /**
     * Bundle coordinates configuration property key.
     */
    public static final String BUNDLE_COORDINATES = "NexusBundleConfiguration.bundleCoordinates";

    /**
     * Constructor.
     *
     * @param bundleCoordinates Maven artifact coordinates of bundle to be resolved. If injected will use the
     *                          coordinates bounded to {@link #BUNDLE_COORDINATES}
     * @param artifactResolver  artifact resolver to be used to resolve the bundle
     * @since 1.10.0
     */
    @Inject
    public NexusBundleResolver( final @Nullable @Named( "${" + BUNDLE_COORDINATES + "}" ) String bundleCoordinates,
                                final MavenArtifactResolver artifactResolver )
    {
        super( bundleCoordinates, artifactResolver );
    }
}
