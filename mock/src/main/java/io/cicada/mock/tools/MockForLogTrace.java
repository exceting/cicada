package io.cicada.mock.tools;

import io.cicada.tools.logtrace.annos.LogTrace;
import io.cicada.tools.logtrace.annos.Slf4jCheck;

import java.beans.JavaBean;

@TestAnno
@JavaBean
@SuppressWarnings({})
@Slf4jCheck
public class MockForLogTrace {

    private static final String v = "xxxfx";

    public static void main(String[] args) {
        MockForLogTrace m = new MockForLogTrace();
        m.testIf();
    }

    static final int VV = 5;

    @LogTrace
    private void testIf() {
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
    }

    private boolean isHid() {
        return true;
    }

    private boolean isHid2(boolean r) {
        return r;
    }

}
