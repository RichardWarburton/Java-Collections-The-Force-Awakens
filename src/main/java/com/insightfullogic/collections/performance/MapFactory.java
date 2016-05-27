package com.insightfullogic.collections.performance;

import com.koloboke.collect.map.hash.HashObjObjMaps;

import java.util.HashMap;
import java.util.Map;

public enum MapFactory
{
    JdkMap
        {
            @Override
            public <T> Map<T, String> make()
            {
                return new HashMap<>(16);
            }
        },
    Koloboke
        {
            @Override
            public <T> Map<T, String> make()
            {
                return HashObjObjMaps.getDefaultFactory().newMutableMap(16);
            }
        };

    public abstract <T> Map<T, String> make();
}
