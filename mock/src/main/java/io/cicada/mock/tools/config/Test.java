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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4jCheck
public class Test {

    public static final AtomicBoolean isOpen = new AtomicBoolean(true);

    static int VV = 5;

    @LogTrace(traceLevel = Level.DEBUG, banLoop = true)
    public static void main(String[] args) {
        try {
            System.out.println("xxx");
        } catch (Exception e) {
            if (e == null) {
                System.out.println("fff");
            }
            switch (e.getMessage()) {
                case "x":
                    break;
                case "b":
                    break;
                default:
            }
        } finally {
            if (args == null || args.length == 0) {
                System.out.println("xxx");
            } else {
                int i = 0;
                while (args[i] != null) {
                    if (Strings.isNullOrEmpty(args[i])) {
                        System.out.println("第" + i + "个元素为空！");
                    }
                    i++;
                }
            }
        }
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

        Demo demo = new Demo();
        new Consumer<String>() {
            List<?> l = Lists.newArrayList();

            @Override
            public void accept(String str) {
                if (Strings.isNullOrEmpty(str)) {
                    List<String> ss = Lists.newArrayList();
                    System.out.println("str is empty " + l);

                    if (new Predicate<Object>() {
                        private List<?> sa = Lists.newArrayList();

                        @Override
                        public boolean test(Object o) {
                            if (o == null) {
                                return false;
                            }
                            return true;
                        }
                    }.test("null")) {
                        System.out.println("Ohhhh my god!");
                    }
                } else {
                    System.out.println(str);
                }
            }
        }.accept(null);

        Consumer<String> consumer = new Consumer<String>() {
            @Override
            public void accept(String o) {
                if (Strings.isNullOrEmpty(o)) {
                    System.out.println("o is empty");
                } else {
                    System.out.println(o);
                }
            }
        };

        consumer.accept("xxxxx");
    }


    static void t(BiFunction<?, ?, ?> b) {
        String ss = (String) b.apply(null, null);
        System.out.println(ss);
    }
}
