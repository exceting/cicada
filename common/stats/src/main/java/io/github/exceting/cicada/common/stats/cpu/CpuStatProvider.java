package io.github.exceting.cicada.common.stats.cpu;

import io.github.exceting.cicada.common.stats.cpu.linux.LinuxOsCpuStat;
import io.github.exceting.cicada.common.stats.cpu.other.OtherOsCpuStat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CpuStatProvider {

    private static final OsCpuStat osCpuStat;

    static {
        String osName = System.getProperty("os.name");
        log.info("CpuStatProvider init, OS name: {}", osName);
        if (osName.equalsIgnoreCase("linux")) {
            osCpuStat = new LinuxOsCpuStat();
        } else {
            osCpuStat = new OtherOsCpuStat();
        }
    }

    public static long getCpu() {
        return osCpuStat.geStat().getUsage();
    }

}
