package io.github.exceting.cicada.tools.cachechain;

import com.google.common.collect.Lists;
import io.github.exceting.cicada.tools.cachechain.cache.CacheChain;
import io.github.exceting.cicada.tools.cachechain.cache.CacheChainClient;
import io.github.exceting.cicada.tools.cachechain.proxy.CacheChainProxy;
import io.github.exceting.cicada.tools.cachechain.refresh.RefreshKeyConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 读取配置，生成CacheChain代理对象
 */
@Slf4j
public class CacheChainRegistry {

    private final List<CacheChainGroup> clients = Lists.newArrayList();
    private final List<Integer> levels = Lists.newArrayList(0);
    private final AtomicInteger currentLevel = new AtomicInteger();
    private final String name;
    private static final AtomicInteger defaultNameCounter = new AtomicInteger();

    public CacheChainRegistry(String name) {
        this.name = name;
    }

    public static CacheChainRegistry build() {
        return new CacheChainRegistry(String.format("CacheChain-%d", defaultNameCounter.incrementAndGet()));
    }

    public static CacheChainRegistry build(String name) {
        return new CacheChainRegistry(name);
    }

    @NonNull
    public CacheChainRegistry registerCache(CacheChainClient cacheChainClient) {
        return registerCache(cacheChainClient, null);
    }

    @NonNull
    public CacheChainRegistry registerCache(CacheChainClient cacheChainClient, RefreshKeyConfig refreshKeyConfig) {
        levels.add(currentLevel.incrementAndGet());
        clients.add(CacheChainGroup.builder()
            .cacheChainClient(cacheChainClient)
            .refreshKeyConfig(refreshKeyConfig)
            .build());
        return this;
    }

    public CacheChain getChain() {
        if (clients.isEmpty()) {
            throw new IllegalStateException("You never registered anything!");
        }
        if (clients.size() + 1 != levels.size()) {
            throw new IllegalStateException("There are duplicate levels!");
        }
        Collections.reverse(clients);
        CacheChain cacheChain = null;
        //排完序后，level最高的应该包在最前面，符合缓存调用序：L3->L2->L1->L0
        for (int i = 0; i < levels.size(); i++) {
            if (cacheChain == null) {
                //↓下面这个是个虚拟chain，位于L1的下游(L0)，最终由它来触发回源方法的调用
                cacheChain = new CacheChainProxy(name, levels.get(i), null, null);
            } else {
                CacheChainGroup currentGroup = clients.get(i - 1);
                cacheChain = new CacheChainProxy(name, levels.get(i), cacheChain, currentGroup.getCacheChainClient(), currentGroup.getRefreshKeyConfig());
            }
        }
        return cacheChain;
    }

    @Getter
    @Builder
    private static class CacheChainGroup {
        private final RefreshKeyConfig refreshKeyConfig;
        private final CacheChainClient cacheChainClient;
    }

}
