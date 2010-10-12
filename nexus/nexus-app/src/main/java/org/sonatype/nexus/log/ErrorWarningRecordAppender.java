package org.sonatype.nexus.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.sonatype.nexus.feeds.ErrorWarningEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.timeline.TimelineException;

/**
 * This class extends log4j, record all error/warning log
 * 
 * @author juven xu
 */
public class ErrorWarningRecordAppender
    extends AppenderSkeleton
{
    private FeedRecorder feedRecorder;

    @Override
    protected void append( LoggingEvent event )
    {
        // we get local instance, just in case another thread comes in and closes the appender
        // (thus nullifying the feedRecorder object) before we attempt to add to it below
        FeedRecorder localRecorder = feedRecorder;
        
        if ( localRecorder == null )
        {
            return;
        }

        // hack to prevent infinite loop
        if ( event.getThrowableInformation() != null
            && event.getThrowableInformation().getThrowable() instanceof TimelineException )
        {
            return;
        }

        String action = "";

        if ( event.getLevel().equals( Level.WARN ) )
        {
            action = ErrorWarningEvent.ACTION_WARNING;
        }
        else if ( event.getLevel().equals( Level.ERROR ) )
        {
            action = ErrorWarningEvent.ACTION_ERROR;
        }
        // TODO: record Level.FATAL ?
        else
        {
            return;
        }

        String message = (String) event.getMessage();

        if ( !shouldIgnore( message, event ) )
        {
            if ( event.getThrowableInformation() != null )
            {
                localRecorder.addErrorWarningEvent( action, message, event.getThrowableInformation().getThrowable() );
            }
            else
            {
                localRecorder.addErrorWarningEvent( action, message );
            }
        }
    }

    @Override
    public void close()
    {
        feedRecorder = null;
    }

    @Override
    public boolean requiresLayout()
    {
        return false;
    }

    public FeedRecorder getFeedRecorder()
    {
        return feedRecorder;
    }

    public void setFeedRecorder( FeedRecorder feedRecorder )
    {
        this.feedRecorder = feedRecorder;
    }

    protected boolean shouldIgnore( String message, LoggingEvent event )
    {
        if ( event != null && event.getThrowableInformation() != null
            && event.getThrowableInformation().getThrowable() != null )
        {
            if ( "org.mortbay.jetty.EofException".equals( event.getThrowableInformation().getThrowable().getClass().getName() ) )
            {
                return true;
            }
        }

        if ( message != null )
        {
            // we don't want to notify in feed about client exceptions closing, this will just annoy everyone
            if ( message.contains( "An exception occured writing the response entity" )
                || message.contains( "Error while handling an HTTP server call" ) )
            {
                return true;
            }
        }

        return false;

    }

}
