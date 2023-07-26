package io.cicada.mock.tools;

import io.cicada.tools.logtrace.annos.LogTrace;
import io.cicada.tools.logtrace.annos.Slf4jCheck;
import org.slf4j.event.Level;

import java.beans.JavaBean;
import java.util.List;

@TestAnno
@JavaBean
@SuppressWarnings({})
@Slf4jCheck
public class MockForLogTrace {

    private static final String v = "xxxfx";

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
        }
    }

    private boolean isHid() {
        return true;
    }

    private boolean isHid2(boolean r) {
        return r;
    }

}
