package io.supermonkey.crawler.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;

/**
 * 
 * @author enijkamp
 *
 */
public class Logs {

	private final static Log LOG = LogFactory.getLog(Logs.class);

    private static Method TRACE;
    private static Method DEBUG;
    private static Method INFO;
    private static Method WARN;
    private static Method ERROR;
    private static Method FATAL;

    static {
        try {
            TRACE = Log.class.getMethod("trace", new Class[]
            { Object.class });
            DEBUG = Log.class.getMethod("debug", new Class[]
            { Object.class });
            INFO = Log.class.getMethod("info", new Class[]
            { Object.class });
            WARN = Log.class.getMethod("warn", new Class[]
            { Object.class });
            ERROR = Log.class.getMethod("error", new Class[]
            { Object.class });
            FATAL = Log.class.getMethod("fatal", new Class[]
            { Object.class });
        }
        catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Cannot init log methods", e);
            }
        }
    }

    public static PrintStream getTraceStream(final Log logger)
    {
        return getLogStream(logger, TRACE);
    }

    public static PrintStream getDebugStream(final Log logger)
    {
        return getLogStream(logger, DEBUG);
    }

    public static PrintStream getInfoStream(final Log logger)
    {
        return getLogStream(logger, INFO);
    }

    public static PrintStream getWarnStream(final Log logger)
    {
        return getLogStream(logger, WARN);
    }

    public static PrintStream getErrorStream(final Log logger)
    {
        return getLogStream(logger, ERROR);
    }

    public static PrintStream getFatalStream(final Log logger)
    {
        return getLogStream(logger, FATAL);
    }

    private static PrintStream getLogStream(final Log logger, final Method method) {
        return new PrintStream(new ByteArrayOutputStream() {
	        private int scan = 0;

            private boolean hasNewline() {
                for (; scan < count; scan++) {
                    if (buf[scan] == '\n')
                        return true;
                }
                return false;
            }

            public void flush() throws IOException {
                if (!hasNewline())
                    return;
                try {
                    method.invoke(logger, new Object[]
                    { toString().replace("\n", "") });
                } catch (Exception e) {
                    if (LOG.isFatalEnabled()) {
                        LOG.fatal("Cannot log with method [" + method + "]", e);
                    }
                }
                reset();
                scan = 0;
            }
        }, true);
    }
}
