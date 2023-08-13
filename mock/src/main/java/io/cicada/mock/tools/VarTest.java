package io.cicada.mock.tools;

import io.cicada.tools.logtrace.annos.MethodLog;
import io.cicada.tools.logtrace.annos.Slf4jCheck;
import io.cicada.tools.logtrace.annos.VarLog;

import java.util.function.Consumer;

@Slf4jCheck
public class VarTest {

    @MethodLog(exceptionLog = true, dur = true)
    public static void main(String[] args) {

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
