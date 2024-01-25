package io.github.exceting.cicada.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class LoggerAdapter implements Logger {

    private final Logger delegateLogger;

    public LoggerAdapter(Class<?> targetClass) {
        this.delegateLogger = LoggerFactory.getLogger(targetClass);
    }

    @Override
    public String getName() {
        return delegateLogger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return delegateLogger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        delegateLogger.trace(LogFormat.trace(msg));
    }

    @Override
    public void trace(String format, Object arg) {
        delegateLogger.trace(LogFormat.trace(format), arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        delegateLogger.trace(LogFormat.trace(format), arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        delegateLogger.trace(LogFormat.trace(format), arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        delegateLogger.trace(LogFormat.trace(msg), t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegateLogger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        delegateLogger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        delegateLogger.trace(marker, LogFormat.trace(format), arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        delegateLogger.trace(marker, LogFormat.trace(format), arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        delegateLogger.trace(marker, LogFormat.trace(format), argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        delegateLogger.trace(marker, LogFormat.trace(msg), t);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegateLogger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        delegateLogger.debug(LogFormat.debug(msg));
    }

    @Override
    public void debug(String format, Object arg) {
        delegateLogger.debug(LogFormat.debug(format), arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        delegateLogger.debug(LogFormat.debug(format), arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        delegateLogger.debug(LogFormat.debug(format), arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        delegateLogger.debug(LogFormat.debug(msg), t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegateLogger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        delegateLogger.debug(marker, LogFormat.debug(msg));
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        delegateLogger.debug(marker, LogFormat.debug(format), arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        delegateLogger.debug(marker, LogFormat.debug(format), arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        delegateLogger.debug(marker, LogFormat.debug(format), arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        delegateLogger.debug(marker, LogFormat.debug(msg), t);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegateLogger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        delegateLogger.info(LogFormat.info(msg));
    }

    @Override
    public void info(String format, Object arg) {
        delegateLogger.info(LogFormat.info(format), arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        delegateLogger.info(LogFormat.info(format), arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        delegateLogger.info(LogFormat.info(format), arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        delegateLogger.info(LogFormat.info(msg), t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegateLogger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        delegateLogger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        delegateLogger.info(marker, LogFormat.info(format), arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        delegateLogger.info(marker, LogFormat.info(format), arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        delegateLogger.info(marker, LogFormat.info(format), arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        delegateLogger.info(marker, LogFormat.info(msg), t);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegateLogger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        delegateLogger.warn(LogFormat.warn(msg));
    }

    @Override
    public void warn(String format, Object arg) {
        delegateLogger.warn(LogFormat.warn(format), arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        delegateLogger.warn(LogFormat.warn(format), arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        delegateLogger.warn(LogFormat.warn(format), arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        delegateLogger.warn(LogFormat.warn(msg), t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegateLogger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        delegateLogger.warn(marker, LogFormat.warn(msg));
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        delegateLogger.warn(marker, LogFormat.warn(format), arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        delegateLogger.warn(marker, LogFormat.warn(format), arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        delegateLogger.warn(marker, LogFormat.warn(format), arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        delegateLogger.warn(marker, LogFormat.warn(msg), t);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegateLogger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        delegateLogger.error(LogFormat.error(msg));
    }

    @Override
    public void error(String format, Object arg) {
        delegateLogger.error(LogFormat.error(format), arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        delegateLogger.error(LogFormat.error(format), arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        delegateLogger.error(LogFormat.error(format), arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        delegateLogger.error(LogFormat.error(msg), t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegateLogger.isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String msg) {
        delegateLogger.error(marker, LogFormat.error(msg));
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        delegateLogger.error(marker, LogFormat.error(format), arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        delegateLogger.error(marker, LogFormat.error(format), arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        delegateLogger.error(marker, LogFormat.error(format), arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        delegateLogger.error(marker, LogFormat.error(msg), t);
    }
}
