package gov.ca.cpuc.calspeed.android;

/**
 * Created by joshuaahn on 8/8/16.
 */

import java.util.HashMap;

/**
 *
 * @author joshuaahn
 */
public class TechnologyCode {
    public static final HashMap<Integer, String> techCodeDictionary =
            populateDictionary();

    public String get(Integer key) {
        return techCodeDictionary.get(key);
    }

    private static HashMap<Integer, String> populateDictionary() {
        HashMap<Integer, String> initializeDictionary = new HashMap<>();
        initializeDictionary.put(0, "All Other");
        initializeDictionary.put(10, "Asymmetric xDSL");
        initializeDictionary.put(11, "ADSL2, ADSL2+");
        initializeDictionary.put(12, "VDSL");
        initializeDictionary.put(20, "Symmetric xDSL");
        initializeDictionary.put(30, "Other Copper Wireline");
        initializeDictionary.put(40, "Cable Modem other");
        initializeDictionary.put(41, "Cable Modem DOCSIS 1, 1.1, 2.0");
        initializeDictionary.put(42, "Cable Modem DOCSIS 3.0");
        initializeDictionary.put(43, "Cable Modem DOCSIS 3.1");
        initializeDictionary.put(50, "Optical Carrier / Fiber to the end user");
        initializeDictionary.put(60, "Satellite");
        initializeDictionary.put(70, "Terrestrial Fixed Wireless");
        initializeDictionary.put(80, "WCDMA/UTMS/HSPA");
        initializeDictionary.put(81, "HSPA+");
        initializeDictionary.put(82, "EVDO/EVDO Rev A");
        initializeDictionary.put(83, "LTE");
        initializeDictionary.put(84, "WiMAX");
        initializeDictionary.put(85, "CDMA");
        initializeDictionary.put(86, "GSM");
        initializeDictionary.put(87, "Analog");
        initializeDictionary.put(88, "Other");
        initializeDictionary.put(89, "Mobile");
        initializeDictionary.put(90, "Electric Power Line");
        initializeDictionary.put(-999, "N/A");
        return initializeDictionary;
    }
}
