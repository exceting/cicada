package io.github.exceting.cicada.common.stats.cpu;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CpuStat {
    private final long cores;
    private final long usage;
}