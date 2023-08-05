package io.cicada.mock.tools;

import io.cicada.mock.tools.config.Test;
import io.cicada.tools.logtrace.annos.Ban;
import io.cicada.tools.logtrace.annos.LogTrace;
import io.cicada.tools.logtrace.annos.Slf4jCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@TestAnno
@Slf4jCheck(isOpen = "io.cicada.mock.tools.config.Test#isOpen")
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

    static final Logger logger = LoggerFactory.getLogger(MockForLogTrace.class);



    @LogTrace(banLoop = true, traceLevel = Level.DEBUG)
    private void testIf(Integer id,
                        List<Integer> names,
                        int[] as,
                        List<String>[] lists,
                        @Ban EmptyInterface emptyInterface) {
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

            boolean sss = isHid();
            Boolean ssss = isHid();
            List sss2 = getList();
            boolean isOpen = Test.isOpen.get();
            boolean sss3 = false;
            int br = isHid() ? 1 : 2;
            boolean br2 = sss == sss3;
            List<String> sss4 = new ArrayList<>();
            Object sss5 = isHid() ? new Object() : null;
            MockForLogTrace sss55 = getMock();
            Map<String, String> sss6 = getMap();
            int[] sss7 = isHid() ? new int[]{1, 2} : null;
            Object[] sss8 = isHid() ? new Object[]{new Object()} : null;
            if (sss5 == null)
                System.out.println("767676");

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
            } else if (i == 2) {
                System.out.println("22222");
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

    private MockForLogTrace getMock() {
        return new MockForLogTrace();
    }

    private List<String> getList() {
        return new ArrayList<>();
    }

    private Map<String, String> getMap() {
        return null;
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
