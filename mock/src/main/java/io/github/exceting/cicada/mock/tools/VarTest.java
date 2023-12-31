package io.github.exceting.cicada.mock.tools;

import io.github.exceting.cicada.tools.logtrace.annos.MethodLog;
import io.github.exceting.cicada.tools.logtrace.annos.Slf4jCheck;
import io.github.exceting.cicada.tools.logtrace.annos.VarLog;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Slf4jCheck
public class VarTest {

    @MethodLog(exceptionLog = true, noThrow = false, dur = true)
    public static void main(String[] args) {

        System.out.println("=======   " + String.valueOf(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE)));

        @VarLog(dur = true)
        int a = 1;

        a = 2;

        if (args == null || args.length == 0) {
            a = 7;
            {
                a = 8;

                int cc = 10;
                cc = 11;
            }
            {
                @VarLog
                int cc = 888;
                cc = 999;
            }
        }

        new Consumer<String>() {

            int a = 0;

            @Override
            public void accept(String s) {
                if (s == null) {
                    int a = 9;
                    a = 10;
                }
            }
        }.accept(null);

        @VarLog
        int b = 5;
        b = 6;

        if ((b = 7) > 6) {
            System.out.println("xxxxx111  " + b);
        }

        System.out.println("xxxxx");
    }

}
