package com.transport.tms.Config;

import com.transport.tms.Constant.TransportConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache(TransportConstants.TRIPS_CACHE),
                new ConcurrentMapCache(TransportConstants.VEHICLE_CACHE),
                new ConcurrentMapCache(TransportConstants.TRIPS_MAP_CACHE)));
        return cacheManager;
    }
}
