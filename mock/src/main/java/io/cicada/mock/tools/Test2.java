package io.cicada.mock.tools;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.cicada.mock.tools.config.Test;
import io.cicada.tools.logtrace.annos.*;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4jCheck(isOpen = "io.cicada.mock.tools.config.Test#isOpen")
public class Test2 {

    static final int VV = 5;

    public static void main(String[] args) {
        Test2 t2 = new Test2();
        t2.t(null, "ssssssssssss");

        System.out.println(String.format("%s%s", 'b', UUID.randomUUID()).replace("-", "_"));

        int a5f719a72_3f91_4678_b4f5_76dba46900d0 = 1;
        System.out.println(a5f719a72_3f91_4678_b4f5_76dba46900d0);
    }

    @MethodLog(exceptionLog = true, dur = true, onlyVar = true, isOpen = "io.cicada.mock.tools.config.Test#isOpen2")
    public void t(@Ban Object o, String s) {
        Object[] sss8 = isHid() ? new Object[]{new Object()} : null;

        for (int i = 0; i < 5; i++) {

        }

        List<Consumer<String>> cs = Lists.newArrayList(new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (!Strings.isNullOrEmpty(s)) {
                    System.out.println(s);
                }
            }
        }, s1 -> {
            if (!Strings.isNullOrEmpty(s1)) {
                System.out.println(s1);
            }
        });

        @VarLog
        Consumer<String>[] cs2 = new Consumer[]{
                new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        if (s != null) {
                            @VarLog(dur = true)
                            int iiii = 566666666;
                            System.out.println(s);
                        }
                    }
                }
        };

        cs2[0].accept("xxee");

        int ia = ((Predicate<String>) input22 -> {
            if (Strings.isNullOrEmpty(input22)) {
                System.out.println("null!");
                return false;
            }
            return true;
        }).apply("sr") ? new Supplier<Integer>() {
            @Override
            public Integer get() {
                if (true) {
                    @VarLog(dur = true)
                    int fvfvfvfvf = 000;
                }
                return 11111;
            }
        }.get() : ((Supplier<Integer>) () -> {
            if (true) {
                @VarLog
                int ghghghghg = 9999;
            }
            return 222222;
        }).get();

        cs2[0].accept("vvvv");

        List<Integer> is = new ArrayList<>(List.of(new Integer[]{0, 2, 4}));
        for (Integer i : is) {
            if (i == 2) {
                System.out.println(i);
            }
        }

        if (((Predicate<Object>) input -> {
            if (input != null) {
                return (Boolean) input;
            } else {
                System.out.println("input is null!!");
            }
            return false;
        }).test(true)) {
            System.out.println("xxxx");
        }

        if (isHid()) {
            System.out.println("ppppppp");
            if (isHid2(false)) {
                System.out.println("ooooooooo");
            }
        }

        if (VV <= 5) { // 常量
            System.out.println("hhhhhh");
        }

        if (new Object() instanceof MockForLogTrace) { // instance of
            System.out.println("uuuuuuuu");
        }

        boolean isOpen = Test.isOpen.get();
        if (o == null)
            System.out.println("767676");
        List<String> stringList = Lists.newArrayList("xxxx");
        if (o == null) {

        } else {
            System.out.println();
        }

        @VarLog
        int t = 2;
        do {
            if (t == 1) {
                System.out.println(222);
            }
            t--;
        } while (t > 0);

        stringList.stream().forEach(i -> {
            if (Strings.isNullOrEmpty(i)) {
            } else {
                System.out.println(i);
            }
        });

        if (isHid2(true)) {
            System.out.println("ooooooo");
        }

        int a = 1;
        int b = 2;
        int add = 0;

        if (add == 3) {
            System.out.println("233333333");
        } else if (add == 4) {
            System.out.println("4444444");
        } else {
            System.out.println("555555");
        }
    }

    @MethodLog(exceptionLog = true, dur = true)
    private boolean isHid() {
        return true;
    }

    private boolean isHid2(boolean r) {
        return r;
    }
}
