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
package org.sonatype.nexus.integrationtests.nexus533;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class AbstractNexusTasksIntegrationIT<E extends ScheduledServiceBaseResource>
    extends AbstractNexusIntegrationTest
{

    protected abstract E getTaskScheduled();

    @BeforeMethod
    public void scheduleTasks()
        throws Exception
    {
        Status status = TaskScheduleUtil.create( getTaskScheduled() );
        AssertJUnit.assertTrue( status.isSuccess() );

        assertTasks();
    }

    protected void assertTasks()
        throws IOException
    {
        Configuration nexusConfig = NexusConfigUtil.getNexusConfig();

        List<CScheduledTask> tasks = nexusConfig.getTasks();
        AssertJUnit.assertEquals( 1, tasks.size() );

        CScheduledTask task = tasks.get( 0 );
        E scheduledTask = getTaskScheduled();

        AssertJUnit.assertEquals( scheduledTask.getName(), task.getName() );
        AssertJUnit.assertEquals( scheduledTask.getTypeId(), task.getType() );
    }

    @Test
    public void updateTasks()
        throws Exception
    {
        E scheduledTask = getTaskScheduled();
        ScheduledServiceListResource task = TaskScheduleUtil.getTask( scheduledTask.getName() );

        scheduledTask.setId( task.getId() );
        updateTask( scheduledTask );
        Status status = TaskScheduleUtil.update( scheduledTask );
        AssertJUnit.assertTrue( status.isSuccess() );

        assertTasks();
    }

    protected abstract void updateTask( E scheduledTask );

    @Test
    public void changeScheduling()
        throws Exception
    {
        E scheduledTask = getTaskScheduled();
        ScheduledServiceListResource task = TaskScheduleUtil.getTask( scheduledTask.getName() );

        // if we have a manual task we can't change the schedule to be manual again
        if ( !task.getSchedule().equals( "manual" ) )
        {

            ScheduledServiceBaseResource taskManual = new ScheduledServiceBaseResource();
            taskManual.setId( task.getId() );
            taskManual.setName( scheduledTask.getName() );
            taskManual.setEnabled( true );
            taskManual.setTypeId( scheduledTask.getTypeId() );
            taskManual.setProperties( scheduledTask.getProperties() );
            taskManual.setSchedule( "manual" );

            Status status = TaskScheduleUtil.update( taskManual );
            AssertJUnit.assertTrue( status.isSuccess() );

        }
        else
        {
            ScheduledServiceOnceResource updatedTask = new ScheduledServiceOnceResource();
            updatedTask.setId( task.getId() );
            updatedTask.setName( scheduledTask.getName() );
            updatedTask.setEnabled( task.isEnabled() );
            updatedTask.setTypeId( scheduledTask.getTypeId() );
            updatedTask.setProperties( scheduledTask.getProperties() );
            updatedTask.setSchedule( "once" );
            Date startDate = DateUtils.addDays( new Date(), 10 );
            startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );
            updatedTask.setStartDate( String.valueOf( startDate.getTime() ) );
            updatedTask.setStartTime( "03:30" );

            Status status = TaskScheduleUtil.update( updatedTask );
            AssertJUnit.assertTrue( status.isSuccess() );
        }

        assertTasks();
    }

    @AfterMethod
    public void deleteTasks()
        throws Exception
    {
        ScheduledServiceListResource task = TaskScheduleUtil.getTask( getTaskScheduled().getName() );
        Status status = TaskScheduleUtil.deleteTask( task.getId() );
        AssertJUnit.assertTrue( status.isSuccess() );

        // delete is not working, see NEXUS-572
        Configuration nexusConfig = NexusConfigUtil.getNexusConfig();
        AssertJUnit.assertTrue( nexusConfig.getTasks().isEmpty() );
    }

}
