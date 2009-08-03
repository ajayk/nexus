/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.scheduling;

import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.scheduling.SchedulerTask;

/**
 * The base interface for all Tasks used in Nexus.
 * 
 * @author cstamas
 * @param <T>
 */
@ExtensionPoint
public interface NexusTask<T>
    extends SchedulerTask<T>
{

    /**
     * Prefix for rpivate properties keys. *
     */
    static final String PRIVATE_PROP_PREFIX = ".";

    /**
     * Key of alert email property (private) *
     */
    static final String ALERT_EMAIL_KEY = PRIVATE_PROP_PREFIX + "alertEmail";

    /**
     * Should an alert email be sent?
     *
     * @return true if alert email is set (not null and not empty), false otherwise
     */
    boolean shouldSendAlertEmail();

    /**
     * Returns the email address to which an email should be sent in case of task failure.<br/>
     * If the alert email is not set (null or empty) no email should be sent.
     *
     * @return alert email
     */
    String getAlertEmail();

    /**
     * Sets the email address to which an email should be sent in case of task failure.<br/>
     * If the alert email is not set (null or empty) no email should be sent.
     *
     * @param email alert email address
     */
    void setAlertEmail( String email );

    TaskActivityDescriptor getTaskActivityDescriptor();
}
