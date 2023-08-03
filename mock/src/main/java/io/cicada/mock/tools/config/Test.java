package io.cicada.mock.tools.config;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.cicada.mock.tools.Demo;
import io.cicada.tools.logtrace.annos.LogTrace;
import io.cicada.tools.logtrace.annos.Slf4jCheck;
import org.slf4j.event.Level;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4jCheck
public class Test {

    public static final AtomicBoolean isOpen = new AtomicBoolean(true);

    static int VV = 5;

    @LogTrace(traceLevel = Level.DEBUG)
    public static void main(String[] args) {
        List<String> ss = Lists.newArrayList();
        List<String> newSS = ss.stream().filter(s -> {
            if (s == null) {
                return false;
            } else {
                return true;
            }
        }).map(s -> {
            if (s == null) {
                System.out.println("空的，转个啥捏？");
            }
            return s;
        }).collect(Collectors.toList());

        Demo.t((t, u) -> {
            if (Strings.isNullOrEmpty(t) || Strings.isNullOrEmpty(u)) {
                System.out.println("t or u is empty!");
                return "";
            }
            return String.format("%s . %s", t, u);
        });
    }


    static void t(BiFunction<?, ?, ?> b) {
        String ss = (String) b.apply(null, null);
        System.out.println(ss);
    }
}
