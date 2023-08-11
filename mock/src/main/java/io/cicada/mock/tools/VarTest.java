package io.cicada.mock.tools;

import java.util.function.Consumer;

public class VarTest {

    public static void main(String[] args) {
        int a = 1;
        {
            int a_2 = 2;
        }
        {
            int a_2 = 3;
            {
                int a_2_3 = 4;
            }
            {
                int a_2_3 = 5;
            }
        }

        new Consumer<>(){

            @Override
            public void accept(Object o) {
                System.out.println(a);
                int a=2;
                System.out.println(a);
            }
        }.accept(null);

        int i = 0;
        if ((i = 1) == 1) {

        }
        {

        }
    }

}
