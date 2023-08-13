package io.cicada.mock.tools;

import io.cicada.tools.logtrace.annos.Slf4jCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Slf4jCheck
public class Empty {
    Logger trace_logger_0 = LoggerFactory.getLogger(Empty.class);
    Object trace_logger = new Object();

    public static class Teacher {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
