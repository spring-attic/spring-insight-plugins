package com.springsource.insight.plugin.ehcache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.Element;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.util.test.MicroBenchmark;

@Category(MicroBenchmark.class)
public class EhcacheBenchmarkTest extends EhcacheOperationCollectionAspectTestSupport {

    private static final int MULTI = 100;
    private static final int RUNS = 5;

    @Test
    public void run() throws Exception {
        long time = 0l;
        long memory = 0l;
        for (int i = 0; i < RUNS; i++) {
            final List<Long> result = runBenchmark();
            time += result.get(0);
            memory += result.get(1);
            manager.clearAll();
        }
        System.out.printf("Total Time %dms , Total Memory Used %dmb , Avg. Time / Run %.2fms Avg. Memory / Run %.2fmb",
                          time,
                          memory,
                          (double) time / RUNS,
                          (double) memory / RUNS);
    }


    private List<Long> runBenchmark() {
        encourageGC();
        final List<KeyValue> keyValues = createKeyValues(5 * MULTI);
        final long startTime = System.currentTimeMillis();
        final long startMemory = Runtime.getRuntime().freeMemory();
        for (int i = 0; i < (3 * MULTI); i++) {
            cache.put(keyValues.get(i).asElement());
        }
        for (int i = 0; i < MULTI; i++) {
            cache.remove(Long.valueOf(i));
        }
        for (int i = 3 * MULTI; i < (5 * MULTI); i++) {
            cache.putIfAbsent(keyValues.get(i).asElement());
        }
        for (int i = 0; i < (4 * MULTI); i++) {
            cache.get(Long.valueOf(i));
        }
        for (int i = 2 * MULTI; i < (3 * MULTI); i++) {
            final KeyValue keyValue = keyValues.get(i);
            keyValue.value = getClass().getName() + "-" + i;
            cache.replace(keyValue.asElement());
        }
        final long endTime = System.currentTimeMillis();
        final long endMemory = Runtime.getRuntime().freeMemory();
        return Arrays.asList((endTime - startTime), ((startMemory - endMemory) / 1024 / 1024));
    }

    private List<KeyValue> createKeyValues(final int amount) {
        final List<KeyValue> keyValues = new ArrayList<KeyValue>(amount);
        for (int i = 0; i < amount; i++) {
            keyValues.add(KeyValue.valueOf(i));
        }
        return keyValues;

    }

    private static final class KeyValue {

        Long key;
        String value;

        KeyValue(final Long key, final String value) {
            this.key = key;
            this.value = value;
        }

        public Element asElement() {
            return new Element(key, value);
        }

        public static KeyValue valueOf(final int i) {
            return new KeyValue(Long.valueOf(i), KeyValue.class.getSimpleName() + "-" + i);
        }
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return new OperationCollectionAspectSupport() {

            @Override
            public String getPluginName() {
                return "";
            }
        };
    }
}
