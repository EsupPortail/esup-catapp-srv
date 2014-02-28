package org.esupportail.catappsrvs.utils.logging;

import fj.*;
import fj.data.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static fj.P.p;
import static org.esupportail.catappsrvs.utils.logging.Log.PassThrough.PassThrough;
import static org.esupportail.catappsrvs.utils.logging.Log.LogLevel.*;

@Slf4j
public enum Log {
    Trace {
        public LogBuilder _(final Object obj, final String method, final Object... inputs) {
            return log.isTraceEnabled()
                    ? new Default(obj, method, inputs, TRACE)
                    : PassThrough;
        }
    },
    Debug {
        public LogBuilder _(final Object obj, final String method, final Object... inputs) {
            return log.isDebugEnabled()
                    ? new Default(obj, method, inputs, DEBUG)
                    : PassThrough;
        }
    },
    Info {
        public LogBuilder _(final Object obj, final String method, final Object... inputs) {
            return log.isInfoEnabled()
                    ? new Default(obj, method, inputs, INFO)
                    : PassThrough;
        }
    };

    public abstract LogBuilder _(Object obj, String method, Object... inputs);

    private static <T> P2<String, Object[]> message(Object obj, String method, T result, Object... inputs) {
        return p(
                "IN: {}, METHOD: {}, INPUT: {}, OUTPUT: {}",
                new Object[] {obj.getClass().getName(), method, inputs, result});
    }

    private static boolean isLevelEnabled(LogLevel logLevel) {
        switch (logLevel) {
            case TRACE:
                return log.isTraceEnabled();
            case DEBUG:
                return log.isDebugEnabled();
            case INFO:
                return log.isInfoEnabled();
            case WARN:
                return log.isWarnEnabled();
            case ERROR:
                return log.isErrorEnabled();
            default:
                return false;
        }
    }

    public interface LogBuilder {
        public <T> T log(P1<T> output);
        public <T> Unit effect(P1<T> output);
    }
    @RequiredArgsConstructor
    protected final class Default implements LogBuilder {
        final Object obj;
        final String method;
        final Object[] inputs;
        final LogLevel logLevel;

        public <T> T log(P1<T> output) {
            final T result = output._1();
            final P2<String, Object[]> message = message(obj, method, result, inputs);
            actions.get(logLevel).foreach(new Effect<F2<String, Object[], Unit>>() {
                public void e(F2<String, Object[], Unit> action) {
                    action.f(message._1(), message._2());
                }
            });
            return result;
        }

        public <T> Unit effect(P1<T> output) {
            if (isLevelEnabled(logLevel))
                log(output);
            return Unit.unit();
        }
    }
    protected enum PassThrough implements LogBuilder {
        PassThrough {
            public <T> T log(P1<T> output) { return output._1(); }
            public <T> Unit effect(P1<T> output) { return Unit.unit(); }
        }
    }

    protected static enum LogLevel { TRACE, DEBUG, INFO, WARN, ERROR }

    private static final TreeMap<LogLevel, F2<String, Object[], Unit>> actions =
            TreeMap.<LogLevel, F2<String, Object[], Unit>>empty(Ord.intOrd.comap(new F<LogLevel, Integer>() {
                public Integer f(LogLevel logLevel) {
                    return logLevel.ordinal();
                }
            }))
            .set(TRACE, new F2<String, Object[], Unit>() {
                public Unit f(String s, Object[] objects) {
                    log.trace(s, objects);
                    return Unit.unit();
                }
            })
            .set(DEBUG, new F2<String, Object[], Unit>() {
                public Unit f(String s, Object[] objects) {
                    log.debug(s, objects);
                    return Unit.unit();
                }
            })
            .set(INFO, new F2<String, Object[], Unit>() {
                public Unit f(String s, Object[] objects) {
                    log.info(s, objects);
                    return Unit.unit();
                }
            })
            .set(WARN, new F2<String, Object[], Unit>() {
                public Unit f(String s, Object[] objects) {
                    log.warn(s, objects);
                    return Unit.unit();
                }
            })
            .set(ERROR, new F2<String, Object[], Unit>() {
                public Unit f(String s, Object[] objects) {
                    log.error(s, objects);
                    return Unit.unit();
                }
            });
}
