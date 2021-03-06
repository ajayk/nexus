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
package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Interface StorageFileItem.
 */
public interface StorageFileItem
    extends StorageItem
{
    /** The digest sha1 key used in item context and attributes. */
    public static final String DIGEST_SHA1_KEY = "digest.sha1";

    /** The digest md5 key used in item context and attributes. @deprecated MD5 is deprecated, use SHA1. */
    @Deprecated
    public static final String DIGEST_MD5_KEY = "digest.md5";

    /**
     * Gets the length.
     * 
     * @return the length
     */
    long getLength();

    /**
     * Sets the length of file.
     * 
     * @param length
     */
    void setLength( long length );

    /**
     * Gets the mime type.
     * 
     * @return the mime type
     */
    String getMimeType();

    /**
     * Shorthand method, goes to ContentLocator. Reusable stream. See {@link ContentLocator}
     * 
     * @return true, if successful
     */
    boolean isReusableStream();

    /**
     * Shorthand method, goes to ContentLocator. Gets the input stream. Caller must close the stream. See
     * {@link ContentLocator}
     * 
     * @return the input stream
     */
    InputStream getInputStream()
        throws IOException;

    /**
     * Sets the content locator.
     */
    void setContentLocator( ContentLocator locator );

    /**
     * Exposes the content locator.
     */
    ContentLocator getContentLocator();

    /**
     * Returns the ID of content generator to be used with this file, or null if not set.
     * 
     * @return
     */
    String getContentGeneratorId();

    /**
     * Set's content generator to be used with this file item. Passing in null removes use of content generator.
     * 
     * @param contentGenerator
     */
    void setContentGeneratorId( String contentGeneratorId );

    /**
     * Returns true if this file item's content is generated on the fly (is not static).
     * 
     * @return
     */
    boolean isContentGenerated();
}
