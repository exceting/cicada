package io.github.exceting.cicada.common.stats.cpu.linux;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.exceting.cicada.common.stats.ScheduledStats;
import io.github.exceting.cicada.common.stats.cpu.CpuStat;
import io.github.exceting.cicada.common.stats.cpu.OsCpuStat;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LinuxOsCpuStat implements OsCpuStat {

    private static final Splitter BLANK_SPLITTER = Splitter.on(" ").omitEmptyStrings().trimResults();
    private static final Splitter COMMA_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
    private static final Splitter COLON_SPLITTER = Splitter.on(":").omitEmptyStrings().trimResults();

    private static final Cgroup cgroup = new Cgroup();
    private long cores;
    private double quota;
    private volatile long usage;
    private volatile double preSystem;
    private volatile double preTotal;

    public LinuxOsCpuStat() {
        init();
        ScheduledStats.scheduleWithFixedDelay(() -> {
            long cpu = refreshCpuUsage();
            if (cpu > 0) {
                usage = cpu;
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void init() {
        try {
            //获取总核数
            int cpuCounts = Runtime.getRuntime().availableProcessors();

            int phyCores = getCores();
            if (phyCores == 0) {
                List<Double> cpuaccts = perCPUUsage();
                phyCores = cpuaccts == null ? 0 : cpuaccts.size();
            }

            cpuCounts = Math.max(cpuCounts, phyCores);

            cores = cpuCounts;
            quota = (double) cores;
            long cq = cpuQuota();
            if (cq > 0) {
                long period = cpuPeriod();
                double limit = (double) cq / (double) period;
                if (limit < quota) {
                    quota = limit;
                }
            }
            preSystem = systemCPUUsage();
            preTotal = totalCPUUsage();
        } catch (Exception e) {
            log.error("Cpu stat init error!", e);
        }
    }

    private long refreshCpuUsage() {
        try {
            double total = totalCPUUsage();
            if (total <= 0) {
                throw new IllegalStateException("Can not parse cgroup cpu usage.");
            }
            double system = systemCPUUsage();
            long u = 0;
            if (system != preSystem) {
                u = (long) (((total - preTotal) * cores * 1e3) / ((system - preSystem) * quota));
            }
            preSystem = system;
            preTotal = total;
            return u;
        } catch (Exception e) {
            log.warn("RefreshCPU error!", e);
        }
        return 0;
    }

    @Override
    public CpuStat geStat() {
        return CpuStat.builder()
            .cores(cores)
            .usage(usage)
            .build();
    }

    private List<Double> perCPUUsage() throws IOException {
        Map<String, String> cgs = cgroup.currentcGroup();
        if (!cgs.isEmpty()) {
            return cgroup.cpuAcctUsagePerCPU(cgs);
        }
        return null;
    }

    private long cpuQuota() throws IOException {
        Map<String, String> cgs = cgroup.currentcGroup();
        if (!cgs.isEmpty()) {
            return cgroup.cpuCFSQuotaUs(cgs);
        }
        return -1;
    }

    private long cpuPeriod() throws IOException {
        Map<String, String> cgs = cgroup.currentcGroup();
        if (!cgs.isEmpty()) {
            return cgroup.cpuCFSPeriodUs(cgs);
        }
        return -1;
    }

    private double totalCPUUsage() throws IOException {
        Map<String, String> cgs = cgroup.currentcGroup();
        if (!cgs.isEmpty()) {
            return cgroup.cpuAcctUsage(cgs);
        }
        return -1;
    }


    private double systemCPUUsage() throws IOException {
        Path path = Paths.get("/proc/stat");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (String line : lines) {
            List<String> parts = BLANK_SPLITTER.splitToList(line);
            if ("cpu".equals(parts.get(0))) {
                if (parts.size() < 8) {
                    throw new IllegalStateException("Bad cpu stats format");
                }
                double totalClockTicks = 0;
                for (int i = 1; i < 8; i++) {
                    totalClockTicks += Double.parseDouble(parts.get(i));
                }
                return totalClockTicks * 1e7;
            }
        }
        throw new IllegalStateException("Bad cpu stats format");
    }

    public int getCores() {
        try {
            Path path = Paths.get("/proc/cpuinfo");
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            int cores = 0;
            if (!lines.isEmpty()) {
                for (String line : lines) {
                    if (line.toLowerCase().startsWith("processor")) {
                        cores++;
                    }
                }
            }
            return cores;
        } catch (IOException e) {
            log.warn("getCores error!", e);
            return 0;
        }
    }

    private static class Cgroup {

        private static final String cgroupRootDir = "/sys/fs/cgroup";

        public Map<String, String> currentcGroup() throws IOException {
            Path path = Paths.get("/proc/self/cgroup");
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            Map<String, String> cgroupMap = Maps.newHashMap();
            for (String line : lines) {
                List<String> col = COLON_SPLITTER.splitToList(line);
                if (col.size() != 3) {
                    continue;
                }
                String dir = col.get(2);
                // 当dir不等于"/"时为docker
                if (!dir.equals("/")) {
                    cgroupMap.put(col.get(1), String.format("%s/%s", cgroupRootDir, col.get(1)));
                    if (col.get(1).contains(",")) {
                        List<String> cols = COMMA_SPLITTER.splitToList(col.get(1));
                        for (String c : cols) {
                            cgroupMap.put(c, String.format("%s/%s", cgroupRootDir, c));
                        }
                    }
                } else {
                    cgroupMap.put(col.get(1), String.format("%s/%s/%s", cgroupRootDir, col.get(1), col.get(2)));
                    if (col.get(1).contains(",")) {
                        List<String> cols = COMMA_SPLITTER.splitToList(col.get(1));
                        for (String c : cols) {
                            cgroupMap.put(c, String.format("%s/%s/%s", cgroupRootDir, c, col.get(2)));
                        }
                    }
                }
            }
            return cgroupMap;
        }

        public List<Double> cpuAcctUsagePerCPU(Map<String, String> cgroupMap) throws IOException {
            Path path = Paths.get(cgroupMap.get("cpuacct"), "cpuacct.usage_percpu");
            List<Double> usage = Lists.newArrayList();
            byte[] contentBytes = Files.readAllBytes(path);
            String content = new String(contentBytes);
            List<String> cpus = BLANK_SPLITTER.splitToList(content);
            for (String cpu : cpus) {
                if (!Strings.isNullOrEmpty(cpu.trim())) {
                    double usageNum = Double.parseDouble(cpu.trim());
                    if (usageNum > 0) {
                        usage.add(usageNum);
                    }
                }
            }
            return usage;
        }

        // CPUCFSQuotaUs cpu.cfs_quota_us
        public long cpuCFSQuotaUs(Map<String, String> cgroupMap) throws IOException {
            Path path = Paths.get(cgroupMap.get("cpu"), "cpu.cfs_quota_us");
            byte[] contentBytes = Files.readAllBytes(path);
            return Long.parseLong(new String(contentBytes, StandardCharsets.UTF_8).trim());
        }

        // CPUCFSPeriodUs cpu.cfs_period_us
        public long cpuCFSPeriodUs(Map<String, String> cgroupMap) throws IOException {
            Path path = Paths.get(cgroupMap.get("cpu"), "cpu.cfs_period_us");
            byte[] contentBytes = Files.readAllBytes(path);
            return Long.parseLong(new String(contentBytes, StandardCharsets.UTF_8).trim());
        }

        // CPUAcctUsage cpuacct.usage
        public double cpuAcctUsage(Map<String, String> cgroupMap) throws IOException {
            Path path = Paths.get(cgroupMap.get("cpuacct"), "cpuacct.usage");
            byte[] contentBytes = Files.readAllBytes(path);
            return Double.parseDouble(new String(contentBytes, StandardCharsets.UTF_8).trim());
        }
    }
}
