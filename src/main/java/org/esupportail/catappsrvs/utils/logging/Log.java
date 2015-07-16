package org.esupportail.catappsrvs.utils.logging;

import fj.*;
import fj.data.Option;
import fj.data.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static fj.P.p;
import static org.esupportail.catappsrvs.utils.logging.Log.LogLevel.*;
import static org.esupportail.catappsrvs.utils.logging.Log.PassThrough.PassThrough;

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
        return p("IN: {}, METHOD: {}, INPUT: {}, OUTPUT: {}",
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
        <T> T log(F0<T> output);
        <T> Unit effect(F0<T> output);
    }
    @RequiredArgsConstructor
    protected final class Default implements LogBuilder {
        final Object obj;
        final String method;
        final Object[] inputs;
        final LogLevel logLevel;

        public <T> T log(F0<T> output) {
            final T result = output.f();
            final P2<String, Object[]> message = message(obj, method, result, inputs);
            actions.maybeGet(logLevel).map(P2::tuple).foreach(Function.apply(message));
            return result;
        }

        public <T> Unit effect(F0<T> output) {
            if (isLevelEnabled(logLevel))
                log(output);
            return Unit.unit();
        }
    }
    protected enum PassThrough implements LogBuilder {
        PassThrough {
            public <T> T log(F0<T> output) { return output.f(); }
            public <T> Unit effect(F0<T> output) { return Unit.unit(); }
        }
    }

    protected enum LogLevel { TRACE, DEBUG, INFO, WARN, ERROR }

    private static final EnumMap<LogLevel, F2<String, Object[], Unit>> actions = EnumMap
        .of(TreeMap.<LogLevel, F2<String, Object[], Unit>>empty(Ord.intOrd.contramap(Enum::ordinal))
                   .set(TRACE, (s, objects) -> {
                       log.trace(s, objects);
                       return Unit.unit();
                   })
                   .set(DEBUG, (s, objects) -> {
                       log.debug(s, objects);
                       return Unit.unit();
                   })
                   .set(INFO, (s, objects) -> {
                       log.info(s, objects);
                       return Unit.unit();
                   })
                   .set(WARN, (s, objects) -> {
                       log.warn(s, objects);
                       return Unit.unit();
                   })
                   .set(ERROR, (s, objects) -> {
                       log.error(s, objects);
                       return Unit.unit();
                   })
                   .toMutableMap());

    private interface SecureMap<K, V> extends Map<K, V> {
        Option<V> maybeGet(K k);
    }

    private static final class EnumMap<K extends Enum<K>, V>
        extends java.util.EnumMap<K, V> implements SecureMap<K, V> {

        private EnumMap(Map<K, ? extends V> m) {  super(m); }
        public static <K extends Enum<K>, V> EnumMap<K, V> of(Map<K, V> map) {
            return new EnumMap<>(map);
        }

        @Override
        public Option<V> maybeGet(K k) { return Option.fromNull(get(k)); }
    }
}
