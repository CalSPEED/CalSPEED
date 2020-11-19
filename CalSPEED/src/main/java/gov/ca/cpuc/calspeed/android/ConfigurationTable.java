package gov.ca.cpuc.calspeed.android;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/*
 * Created by joshuaahn on 9/19/19.
 *
 * This class is creates a dictionary of
 */


class ConfigurationTable {
    private Map<Integer, Map<String, Integer>> configTable;
    private Map<String, Integer> defaultConfigTable;
    private Map<String, Integer> prelimTable;

    // REGULAR PRELIM TESTING
    private final String PRELIM_VALUE = String.format("%s:1,%s:256,%s:10",
            Constants.THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME);

    // DEFAULT CONFIGURATION
    private final String  MOBILE_DEFAULT_VALUE = String.format("%s:1,%s:256,%s:%d",
            Constants.THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME,
            Constants.DEFAULT_TEST_TIME);

    private final int[] TIERS = new int[] {
            10000,
            100000,
            250000,
            Integer.MAX_VALUE
    };
    private final int[] THREAD_NUMS = new int[] {
            1,
            4,
            8,
            8
    };
    private final int[] WINDOW_SIZES = new int[] {
            256,
            256,
            256,
            512
    };

    ConfigurationTable() {
        createPrelimTable();
        createTcpTable();
        createDefaultsTable();
    }

    private void createTcpTable () {
        this.configTable = new TreeMap<>();
        int tierSize = TIERS.length;
        if ((tierSize != WINDOW_SIZES.length) && (tierSize != THREAD_NUMS.length)) {
            Log.w(getClass().getSimpleName(), "Config sizes are incorrect size: " +
                    "tier size: " + TIERS.length + "; thread num: " + THREAD_NUMS.length +
                    "; window size: " + WINDOW_SIZES.length);
        }
        for (int i = 0; i < TIERS.length; i++) {
            int tier = TIERS[i];
            int theadNumber = THREAD_NUMS[i];
            int windowSize = WINDOW_SIZES[i];
            int testTime = Constants.DEFAULT_TEST_TIME;
            String MOBILE_CONFIG_TEMPLATE = "%s:%d,%s:%d,%s:%d";
            String tierValue = String.format(MOBILE_CONFIG_TEMPLATE, Constants.THREAD_NUMBER,
                    theadNumber, Constants.WINDOW_SIZE, windowSize, Constants.TEST_TIME, testTime);
            configTable.put(tier, stringToMap(tierValue));
        }
    }

    private void createDefaultsTable() {
        this.defaultConfigTable = new HashMap<>();
        defaultConfigTable = stringToMap(MOBILE_DEFAULT_VALUE);
    }

    private void createPrelimTable() {
        prelimTable = stringToMap(PRELIM_VALUE);
    }

    private Map<String, Integer> stringToMap(String parseString) {
        Map<String, Integer> stepMap = new HashMap<>();
        String[] pairs = parseString.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            stepMap.put(keyValue[0], Integer.valueOf(keyValue[1]));
        }
        return stepMap;
    }

    Map<Integer, Map<String, Integer>> getTable() {
        return this.configTable;
    }

    Map<String, Integer> getDefaultsTable() {
        return this.defaultConfigTable;
    }

    Map<String, Integer> getPrelimTable() {
        return this.prelimTable;
    }


    @NotNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Preliminary Configuration:\n");
        for (String prelimKey : prelimTable.keySet()) {
            sb.append("\t").append(prelimKey).append(": ");
            sb.append(prelimTable.get(prelimKey)).append("\n");
        }
        sb.append("\nTCP Test Configuration:\n");
        for (Integer key: configTable.keySet()) {
            sb.append("\t").append(key).append(": ").append("\n");
            Map<String, Integer> configs = configTable.get(key);
            if (configs != null) {
                for (String configType : configs.keySet()) {
                    sb.append("\t\t").append(configType).append(": ");
                    sb.append(configs.get(configType)).append("\n");
                }
            }
        }
        sb.append("\nDefault TCP Test Configuration:\n");
        for (String key: defaultConfigTable.keySet()) {
            Integer config = defaultConfigTable.get(key);
            sb.append("\t").append(key).append(": ").append(config).append("\n");
        }
        return sb.toString();
    }
}
