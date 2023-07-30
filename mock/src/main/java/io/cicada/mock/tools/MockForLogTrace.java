package io.cicada.mock.tools;

import io.cicada.tools.logtrace.annos.LogTrace;
import io.cicada.tools.logtrace.annos.Slf4jCheck;
import org.slf4j.event.Level;

import java.beans.JavaBean;
import java.util.ArrayList;
import java.util.List;

@TestAnno
@JavaBean
@SuppressWarnings({})
@Slf4jCheck
public class MockForLogTrace {

    private static final String v = "xxxfxx";

    public static void main(String[] args) {
        MockForLogTrace m = new MockForLogTrace();
        m.testException();
        m.testIf(1, null, new int[]{1, 2}, null, new EmptyInterface() {
        });
    }

    static final int VV = 5;

    // @LogTrace(traceLevel = Level.DEBUG)
    private void testException() {

    }

    @LogTrace(exceptionLog = true, traceLoop = true, traceLevel = Level.DEBUG)
    private void testIf(Integer age, List<Integer> names, int[] as, List<String>[] lists, EmptyInterface emptyInterface) {
        try {
            List<Integer> is = new ArrayList<>(List.of(new Integer[]{0, 2, 4}));
            for (Integer i : is) {
                if (i == 2) {
                    System.out.println(i);
                }
            }

            is.forEach(i -> {
                System.out.println(i);
            });

            int aaa = 2;
            while (aaa > 0) {
                if (aaa == 1) {
                    System.out.println(11111);
                }
                System.out.println(aaa--);
            }

            int t = 2;
            do {
                if (t == 1) {
                    System.out.println(222);
                }
                t--;
            } while (t > 0);

            int i = 0;
            if (i == 0) { // 变量
                System.out.println("cccccc");
            }
            if (VV <= 5) { // 常量
                System.out.println("hhhhhh");
            }

            if (new Object() instanceof MockForLogTrace) { // instance of
                System.out.println("uuuuuuuu");
            }

            if (isHid()) {
                System.out.println("ppppppp");
                if (isHid2(false)) {
                    System.out.println("ooooooooo");
                }
            }

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
        } catch (Exception e) {
            // do nothing
            throw e;
        }
    }

    private boolean isHid() {
        try {
            try {
                return true;
            } catch (Exception e) {
                System.out.println("第一个catch");
                throw e;
            }
        } catch (Exception e) {
            System.out.println("第二个catch");
            throw e;
        }
    }

    private boolean isHid2(boolean r) {
        return r;
    }

}
