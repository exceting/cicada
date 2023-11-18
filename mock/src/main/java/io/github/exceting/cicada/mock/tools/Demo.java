package io.github.exceting.cicada.mock.tools;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.github.exceting.cicada.tools.logtrace.annos.Ban;
import io.github.exceting.cicada.tools.logtrace.annos.MethodLog;
import io.github.exceting.cicada.tools.logtrace.annos.Slf4jCheck;
import io.github.exceting.cicada.tools.logtrace.annos.VarLog;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4jCheck
public class Demo {

    //@LogTrace(exceptionLog = true, banLoop = true, traceLevel = Level.DEBUG)

    //static final Logger logger = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) {
        Demo demo = new Demo();
        demo.demoMethod(11, "hehehe",
                Lists.newArrayList("yyyyy", "uuuuuu"),
                new String[]{"xxx"},
                Lists.newArrayList(new Object[]{new Object()}, new Object[]{new Object()}),
                null,
                null, Kind.ROOT);
        demo.ttttt(null);
    }

    public static void t(BiFunction<String, String, String> bf) {
        String r = bf.apply(null, "2nd param");
        if (!Strings.isNullOrEmpty(r)) {
            System.out.println(r);
        }
    }

    @MethodLog
    public void ttttt(Integer aaa) {
        synchronized (new Consumer<>() {
            @Override
            public void accept(Object o) {
                if (o == null) {
                    System.out.println("lock o is null!");
                }
            }
        }) {
            if (aaa == null) {
                System.out.println("aaa is null!");
            }
        }
    }

    @MethodLog(dur = true)
    public void demoMethod(@Ban int id,
                           @Ban String name,
                           List<String> books,
                           String[] infos,
                           List<Object[]> listArray,
                           @Ban List<List<Map<String, String>>> llm,
                           Object obj,
                           Kind kind) {

        final List<String> final_books = books;

        if (id <= 0 || Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("参数不合法！");
        }
        books.stream().filter(b -> {
            if (b == null) {
                return false;
            }
            return true;
        }).collect(Collectors.toList()).stream().forEach(b -> {
            if (b == null) {
                System.out.println("xxxx");
            }
        });
        books = books == null ? getDefaultBooks() : books;

        @VarLog
        String[] defaultInfos = getDefaultInfos();
        //infos = defaultInfos;

        for (String info : infos) {
            if (Strings.isNullOrEmpty(info)) {
                info = getDefaultInfo();
            }
        }

        @VarLog
        Consumer<String> cccccc = books == null ? new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (Strings.isNullOrEmpty(s)) {
                    System.out.println("三元表达式  条件1  s为空！");
                }
            }
        } : new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (Strings.isNullOrEmpty(s)) {
                    System.out.println("三元表达式  条件2  s为空！");
                }
            }
        };
        cccccc.accept(null);

        int i = 0;
        while (i < infos.length) {
            if (Strings.isNullOrEmpty(infos[i])) {
                infos[i] = getDefaultInfo();
            }
            i++;
        }

        if (id < 50) {
            System.out.println("id in 1~49");
        } else if (id < 100) {
            System.out.println("id in 51~99");
        } else {
            System.out.println("id > 100");
        }

        switch (id) {
            case 1:
                System.out.println("x1");
                break;
            case 11:
                System.out.println("x11");
                break;
            case 5:
                System.out.println("x3");
                break;
            default:
                // do nothing
        }

        switch (kind.name()) {
            case "IMPORT":
                break;
            case "ROOT":
                break;
            default:
        }
    }

    public enum Kind {
        ROOT(),
        IMPORT(),
        CLASS_DECL(),
        METHOD_DECL()
    }

    private List<String> getDefaultBooks() {
        List<String> defaultBooks = Lists.newArrayList();
        defaultBooks.add("book1");
        defaultBooks.add("book2");
        return defaultBooks;
    }

    private String[] getDefaultInfos() {
        return new String[]{"info1", "info2"};
    }

    private String getDefaultInfo() {
        return "xxxx";
    }

}
