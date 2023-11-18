package io.github.exceting.cicada.mock.tools;

import io.github.exceting.cicada.tools.logtrace.annos.MethodLog;
import io.github.exceting.cicada.tools.logtrace.annos.Slf4jCheck;
import io.github.exceting.cicada.tools.logtrace.annos.VarLog;

@Slf4jCheck
public class Test2 {

    public static void main(String[] args) {
        //rt();
        //rt2(new Student());

        System.out.println(minSubArrayLen(7, new int[]{2,3,1,2,4,3,7}));
    }

    @MethodLog
    public static int minSubArrayLen(int target, int[] nums) { //209
        if (nums == null || nums.length == 0) {
            return 0;
        }

        int r = nums.length + 1;

        int start = 0;
        int end = 1;
        while (start < nums.length) {
            int sum = 0;
            for (int j = start; j < end; j++) {
                sum += nums[j];
            }
            System.out.printf("======  s = %s, e = %s, sum = %s\n", start, end, sum);
            if (sum >= target) {
                int nr = end - start;
                if (r > nr) {
                    r = nr;
                }
                start++;
            }
            if (end >= nums.length) {
                start++;
            }
            if (end < nums.length) {
                end++;
            }
        }

        return r == nums.length + 1 ? 0 : r;
    }

    public static int minSubArrayLen2(int target, int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        int r = nums.length + 1;
        for (int i = 0; i < nums.length; i++) {
            int sum = 0;
            int k = 0;
            for (int j = i; j < nums.length; j++) {
                sum = sum + nums[j];
                k++;
                if (sum >= target) {
                    if (r > k) {
                        r = k;
                    }
                    break;
                }

            }
        }
        return r == nums.length + 1 ? 0 : r;
    }

    @MethodLog(exceptionLog = true, dur = true)
    static Student rt() {
        @VarLog
        Student student = new Student();
        student.setId(1);
        student.setName("ssssss");
        System.out.println("xxxxx a = " + student);
        return student;
    }

    @MethodLog(exceptionLog = true, dur = true)
    static Student rt2(Student student) {
        student.setId(1);
        student.setName("ssssss");
        System.out.println("xxxxx a = " + student);
        return student;
    }

    static class Student {
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
