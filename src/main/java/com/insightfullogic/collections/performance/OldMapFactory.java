package com.insightfullogic.collections.performance;

import com.koloboke.collect.hash.HashConfig;
import com.koloboke.collect.map.hash.HashObjObjMaps;

import java.util.HashMap;
import java.util.Map;

public enum OldMapFactory
{
    OpenHashMap
        {
            @Override
            public <T> Map<T, String> make(final float loadFactor)
            {
                return new OpenHashMap<>(16, loadFactor);
            }
        },
    JdkMap
        {
            @Override
            public <T> Map<T, String> make(final float loadFactor)
            {
                return new HashMap<>(16, loadFactor);
            }
        },
    Koloboke
        {
            @Override
            public <T> Map<T, String> make(final float loadFactor)
            {
                HashConfig config = HashConfig.getDefault().withMaxLoad(loadFactor);
                return HashObjObjMaps.getDefaultFactory().withHashConfig(config).newMutableMap(16);
            }
        };

    public abstract <T> Map<T, String> make(final float loadFactor);
}
