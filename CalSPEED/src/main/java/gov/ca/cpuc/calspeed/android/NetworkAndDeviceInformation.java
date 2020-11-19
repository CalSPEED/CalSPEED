package gov.ca.cpuc.calspeed.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Created by joshuaahn on 11/18/16.
 */

class NetworkAndDeviceInformation {
    private final Context context;
    private final AndroidUiServices uiServices;
    private boolean isConnected;
    private String simOperatorCode;
    private String simOperatorName;
    private int networkType;
    private String networkOperatorCode;
    private String networkOperatorName;
    private int locationAreaCode;
    private int cellTowerId;
    private int signalStrength;
    private int snr;
    private int lteSnr;
    private String message = "";

    private ConnectivityManager connectivityManager;
    private final CurrentPhoneStateListener psListener;
    private final TelephonyManager telephonyManager;
    private List<CellInfo> allCellInfo;


    NetworkAndDeviceInformation(Context context, AndroidUiServices uiServices,
                                TelephonyManager telephonyManager,
                                ConnectivityManager connectivityManager) {
        this.context = context;
        this.uiServices = uiServices;
        this.connectivityManager = connectivityManager;
        this.psListener = new CurrentPhoneStateListener();
        this.telephonyManager = telephonyManager;
        initialize();
    }

    private void initialize() {
        Log.i("NetworkAndDeviceInfo", "Connectivity active network info: " + (connectivityManager == null));
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.i("NetworkAndDeviceInfo", "Connectivity active network info: " + (connectivityManager == null));
        Log.i("NetworkAndDeviceInfo", "Telephony Manager: " + (telephonyManager == null));
        if (connectivityManager.getActiveNetworkInfo() == null || telephonyManager == null) {
            Log.i("NetworkAndDeviceInfo", "No Network Available");
            this.isConnected = false;
        } else {
            Log.d("NetworkAndDeviceInfo", "Network Available");
            telephonyManager.listen(psListener,
                    PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
                            PhoneStateListener.LISTEN_CALL_STATE |
                            PhoneStateListener.LISTEN_CELL_LOCATION |
                            PhoneStateListener.LISTEN_DATA_ACTIVITY |
                            PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                            PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
                            PhoneStateListener.LISTEN_SERVICE_STATE |
                            PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            this.isConnected = connectivityManager.getActiveNetworkInfo().isConnected();
            this.simOperatorCode = telephonyManager.getSimOperator();
            this.simOperatorName = telephonyManager.getSimOperatorName();
            updateNetworkType();
            this.networkOperatorCode = telephonyManager.getNetworkOperator();
            this.networkOperatorName = telephonyManager.getNetworkOperatorName();
            this.locationAreaCode = -1;
            this.cellTowerId = -1;
            this.snr = 0;
            handleCellInfo();
        }
    }

    String getMessage() {
        return message;
    }

    private String getNetworkType() {
        Log.v("GetNetworkType", "Getting network type: " + Constants.NETWORK_TYPE[this.networkType][1]);
        return Constants.NETWORK_TYPE[this.networkType][1];
    }

    private void updateNetworkType() {
        Log.v("UpdateNetworkType", "Prev: " + this.networkType + ", Current: " + telephonyManager.getNetworkType());
        this.networkType = telephonyManager.getNetworkType();
    }

    int getLocationAreaCode() {
        return this.locationAreaCode;
    }

    int getCellTowerId() {
        return this.cellTowerId;
    }

    int getSignalStrength() {
        return this.signalStrength;
    }

    int getSnr() {
        if (Objects.equals(getNetworkType(), "LTE")) {
            return this.lteSnr;
        } else {
            return this.snr;
        }
    }


    private void handleCellInfo() {
        allCellInfo = telephonyManager.getAllCellInfo();
        if ((allCellInfo != null)) {
            Log.d("CellInfoSize", "All Cell: " + allCellInfo.size());
            if (allCellInfo.size() > 0) {
                Log.d("CellInfo", "Use allCellInfo");
                handleAllCellInfo();
            } else {
                Log.d("CellInfo", "Use None Cell Info");
            }
            updateNetworkType();
            uiServices.updateNetworkInfo();
        }
    }

    private void handleAllCellInfo() {
        if ((allCellInfo == null) || (allCellInfo.size() == 0)) {
            Log.e("NetworkAndDeviceInfo", "AllCellInfos is null or zero");
        } else {
            if (allCellInfo.size() > 0) {
                ArrayList<Integer> cellInfoSignalStrength = new ArrayList<>();
                HashMap<Integer, Integer> cellInfoTowerAndAreaCode =
                        new HashMap<>();
                for (CellInfo cellInfo : allCellInfo) {
                    CellSignalStrength cellSignalStrength;
                    if (cellInfo instanceof CellInfoCdma) {
                        CellInfoCdma cdmaCellInfo = (CellInfoCdma) cellInfo;
                        CellIdentityCdma cdmaId = cdmaCellInfo.getCellIdentity();
                        Log.v("CDMA", "CDMA Base Station ID: " + cdmaId.getBasestationId());
                        Log.v("CDMA", "CDMA Network ID: " + cdmaId.getNetworkId());
                        Log.v("CDMA", "CDMA System ID: " + cdmaId.getSystemId());
                        cellInfoTowerAndAreaCode.put(cdmaId.getBasestationId(),
                                cdmaId.getNetworkId());
                        CellSignalStrengthCdma cellSignalStrengthCdma =
                                cdmaCellInfo.getCellSignalStrength();
                        Log.v("CDMA","CDMA dBm: " + cellSignalStrengthCdma.getDbm());
                        Log.v("CDMA","CDMA ASU: " + cellSignalStrengthCdma.getAsuLevel());
                        Log.v("CDMA","CDMA level: " + cellSignalStrengthCdma.getLevel());
                        if (isValidSignal(cellSignalStrengthCdma)) {
                            cellInfoSignalStrength.add(
                                    Math.min(
                                            Math.min(cellSignalStrengthCdma.getCdmaDbm(),
                                                    cellSignalStrengthCdma.getEvdoDbm()),
                                            cellSignalStrengthCdma.getDbm()));
                        }
                    } else if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm gsmCellInfo = (CellInfoGsm) cellInfo;
                        CellIdentityGsm gsmId = gsmCellInfo.getCellIdentity();
                        Log.v("GSM", "LAC: " + gsmId.getLac());
                        Log.v("GSM", "CID: " + gsmId.getCid());
                        cellInfoTowerAndAreaCode.put(gsmId.getCid(), gsmId.getLac());
                        cellSignalStrength = gsmCellInfo.getCellSignalStrength();
                        Log.v("GSM", "dBm: " + cellSignalStrength.getDbm());
                        Log.v("GSM","ASU: " + cellSignalStrength.getAsuLevel());
                        Log.v("GSM","Level: " + cellSignalStrength.getLevel());
                        cellInfoSignalStrength.add(convertGsmAsu(cellSignalStrength.getDbm()));
                    } else if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte lteCellInfo = (CellInfoLte) cellInfo;
                        CellIdentityLte lteId = lteCellInfo.getCellIdentity();
                        Log.v("LTE", "CID: " + lteId.getCi());
                        Log.v("LTE", "Physical Cell ID: " + lteId.getPci());
                        Log.v("LTE", "Tracking Area Code: " + lteId.getTac());
                        cellInfoTowerAndAreaCode.put(lteId.getCi(), lteId.getTac());
                        cellSignalStrength = lteCellInfo.getCellSignalStrength();
                        cellInfoSignalStrength.add(cellSignalStrength.getDbm());
                        Log.v("LTE","dBm: " + cellSignalStrength.getDbm());
                        Log.v("LTE","ASU: " + cellSignalStrength.getAsuLevel());
                        Log.v("LTE","Level: " + cellSignalStrength.getLevel());
                        CellSignalStrengthLte cellSignalStrengthLte = (CellSignalStrengthLte) cellSignalStrength;
                        Log.d("LTE", "Stuff: " + cellSignalStrengthLte.toString());
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        CellInfoWcdma wcdmaCellInfo = (CellInfoWcdma) cellInfo;
                        CellIdentityWcdma wcdmaId = wcdmaCellInfo.getCellIdentity();
                        Log.v("WCDMA", "CID: " + wcdmaId.getCid());
                        Log.v("WCDMA", "LAC: " + wcdmaId.getLac());
                        Log.v("WCDMA", "PSC: " + wcdmaId.getPsc());
                        cellInfoTowerAndAreaCode.put(wcdmaId.getCid(), wcdmaId.getLac());
                        cellSignalStrength = wcdmaCellInfo.getCellSignalStrength();
                        cellInfoSignalStrength.add(cellSignalStrength.getDbm());
                        Log.v("WCDMA","dBm: " + cellSignalStrength.getDbm());
                        Log.v("WCDMA","ASU: " + cellSignalStrength.getAsuLevel());
                        Log.v("WCDMA","level: " + cellSignalStrength.getLevel());
                    }
                    Log.v("CIDsLACs",
                            String.valueOf(Collections.singletonList(cellInfoTowerAndAreaCode)));
                    HashSet<Integer> cellIds =
                            new HashSet<>(cellInfoTowerAndAreaCode.keySet());
                    HashSet<Integer> areaCodes =
                            new HashSet<>(cellInfoTowerAndAreaCode.values());
                    Log.v("CellInfoCIDs", String.valueOf(Collections.singletonList(cellIds)));
                    Log.v("CellInfoAreaCodes",
                            String.valueOf(Collections.singletonList(areaCodes)));
                    if (!cellIds.contains(this.cellTowerId)) {
                        this.cellTowerId = Collections.max(cellIds);
                    }
                    Integer areaCode = cellInfoTowerAndAreaCode.get(this.cellTowerId);
                    if (areaCode != null) {
                        this.locationAreaCode = areaCode;
                    }
                    Log.v("LAC", "this location area code: " + this.locationAreaCode);
                    Log.v("CellInfoSignalStrengths",
                            String.valueOf(Collections.singletonList(cellInfoSignalStrength)));
                    if (this.signalStrength == Integer.MAX_VALUE) {
                        this.signalStrength = Collections.min(cellInfoSignalStrength);
                    }
                }
            } else {
                Log.i("CellInfo", "Cell Information list is empty");
            }
        }
    }

    private boolean isValidSignal(CellSignalStrengthCdma signalStength) {
        int cdma = signalStength.getCdmaDbm();
        int evdo = signalStength.getEvdoDbm();
        int dbm = signalStength.getDbm();
        return !(cdma >= -1 || evdo >= -1 && dbm >= -1) && !(cdma == Integer.MIN_VALUE
                || evdo == Integer.MIN_VALUE && dbm == Integer.MIN_VALUE);
    }

    private void handleSignalStrengthChanged(SignalStrength signalStrength) {
        int updatedSignalStrength = Integer.MAX_VALUE;
        int gsmStrength = signalStrength.getGsmSignalStrength();
        int cdmaStrength = signalStrength.getCdmaDbm();
        int evdoStrength = signalStrength.getEvdoDbm();
        handleCellInfo();
        Log.v("GSM_SIGNAL", "GSM Strength: " + convertGsmAsu(gsmStrength));
        if (signalStrength.isGsm() && gsmStrength != 99 && gsmStrength > -120) {
            Log.v("handleSignalStrength", "GSM ASU: " + signalStrength.getGsmSignalStrength());
            updatedSignalStrength = convertGsmAsu(gsmStrength);
            Log.d("handleSignalStrength", "GSM BitErrorRate : " + signalStrength.getGsmBitErrorRate());
            this.snr = signalStrength.getGsmBitErrorRate();
            message += "GSM\n";
        }
        Log.v("CDMA_SIGNAL", "CDMA Strength:" + cdmaStrength);
        Log.v("EVDO_SIGNAL", "EVDO Strength:" + evdoStrength);
        if ((cdmaStrength <= -40 || evdoStrength <= -40) &&
                (cdmaStrength > -120 || evdoStrength > -120)) {
            if ((cdmaStrength <= evdoStrength) || (evdoStrength <= -120)) {
                Log.v("handleSignalStrength", "CDMA dBm: " + cdmaStrength);
                updatedSignalStrength = cdmaStrength;
                Log.d("handleSignalStrength", "CDMA EC/IO : " + signalStrength.getCdmaEcio());
                message += "CDMA\n";
                this.snr = signalStrength.getCdmaEcio();
            } else if ((evdoStrength <= cdmaStrength) || (cdmaStrength <= -120)) {
                Log.v("handleSignalStrength", "EVDO dBm: " + evdoStrength);
                updatedSignalStrength = evdoStrength;
                Log.d("handleSignalStrength", "EVDO SNR : " + signalStrength.getEvdoSnr());
                Log.d("handleSignalStrength", "EVDO EC/IO : " + signalStrength.getEvdoEcio());
                message += String.format("EVDO\nECIO: %d\nSNR: %d\n",
                        signalStrength.getEvdoEcio(), signalStrength.getEvdoSnr());
                this.snr = signalStrength.getEvdoEcio();
            }
        }
        if (isLte(signalStrength)) {
            updatedSignalStrength = getLteSignalStrength(signalStrength);
            Log.v("LTE_SIGNAL", "LTE strength: " + updatedSignalStrength);
        }
        Log.v("handleSignalStrength", "signalStrength is: " + updatedSignalStrength);
        if (updatedSignalStrength != Integer.MAX_VALUE &&
                updatedSignalStrength >= -120 &&
                updatedSignalStrength <= -40) {
            this.signalStrength = updatedSignalStrength;
        }
        Log.v("onSignalStrengthChanged", "Final Signal strength is: " + this.signalStrength);
        Log.v("onSignalStrengthChanged", "Network info is: " + this.locationAreaCode
                + ", " + this.cellTowerId);
        updateNetworkType();
        uiServices.updateNetworkInfo();
    }

    private boolean isLte(SignalStrength signalStrength) {
        try {
            Method[] methods = SignalStrength.class.getMethods();
            for (Method mthd : methods) {
                if (mthd.getName().equals("getLteRsrp")) {
                    int rsrp = (Integer) mthd.invoke(signalStrength);
                    return rsrp < -43 && rsrp >= -140;
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getLteSignalStrength(SignalStrength signalStrength) {
        int rsrp = Integer.MAX_VALUE;
        int rsrq;
        int sinr = Integer.MAX_VALUE;
        try {
            message += String.format("LTE\nNetwork Type: %s\n", getNetworkType());
            Method[] methods = SignalStrength.class.getMethods();
            for (Method mthd : methods) {
                if (mthd.getName().equals("getLteRsrp")) {
                    rsrp = (Integer) mthd.invoke(signalStrength);
                } else if (mthd.getName().equals("getLteRsrq")) {
                    rsrq = (Integer) mthd.invoke(signalStrength);
                } else if (mthd.getName().equals("getLteRssnr")) {
                    this.lteSnr = (Integer) mthd.invoke(signalStrength);
                    Log.v("handleSignalStrength", "LTE SNR is: " + this.snr);
                }
            }
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return rsrp;
    }

    private int convertGsmAsu(int asuValue) {
        return (2 * asuValue) - 113;
    }

    private void updateCellLocation(CellLocation location) {
        if (location == null) {
            Log.w("CellLocation", "Cell Location was not determined");
        } else if (location instanceof GsmCellLocation) {
            Log.v("GSMCellLocation", "Updating cell location to GSM");
            GsmCellLocation gsmLocation = (GsmCellLocation) location;
            Log.v("GSMCellLocation", "GSM CID: " + gsmLocation.getCid());
            Log.v("GSMCellLocation", "GSM LAC: " + gsmLocation.getLac());
            Log.v("GSMCellLocation", "GSM PSC : " + gsmLocation.getPsc());
            this.cellTowerId = gsmLocation.getCid();
            this.locationAreaCode = gsmLocation.getLac();
        } else if (location instanceof CdmaCellLocation) {
            Log.v("CDMACellLocation", "Updating cell location to CDMA");
            CdmaCellLocation cdmaLocation = (CdmaCellLocation) location;
            Log.v("CDMACellLocation", "CDMA Base Station ID: " + cdmaLocation.getBaseStationId());
            Log.v("CDMACellLocation", "CDMA System ID: " + cdmaLocation.getSystemId());
            Log.v("CDMACellLocation", "CDMA Network ID : " + cdmaLocation.getNetworkId());
            this.locationAreaCode = cdmaLocation.getNetworkId();
        }
        updateNetworkType();
        uiServices.updateNetworkInfo();
    }

    private class CurrentPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            handleSignalStrengthChanged(signalStrength);
        }

        @Override
        public void onCellInfoChanged(List<CellInfo> info) {
            super.onCellInfoChanged(info);
            Log.d("CellInfoChanged", "Discovered a change in cell information.");
            handleCellInfo();
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            Log.d("CellLocationChanged", "Discovered a change in cell location");
            updateCellLocation(location);
        }
    }

    @Override
    public String toString() {
        String result = "";
        result += "Device Manufacturer: " + Build.MANUFACTURER + "\n";
        result += "Device Model: " + Build.MODEL + "\n";
        result += "Is Connected: " + this.isConnected + "\n";
        result += "SIM Operator Code: " + this.simOperatorCode + "\n";
        result += "SIM Operator Name: " + this.simOperatorName + "\n";
        result += "Network Type Code: " + this.networkType + "\n";
        result += "Network Operator Code: " + this.networkOperatorCode + "\n";
        result += "Network Operator Name: " + this.networkOperatorName + "\n";
        result += "Location Area Code: " + this.locationAreaCode + "\n";
        result += "Cell Tower ID: " + this.cellTowerId + "\n";
        result += "Signal Type: " + Constants.NETWORK_TYPE[this.networkType][1] + "\n";
        result += "Signal Strength: " + this.signalStrength + "\n";
        return result;
    }

}
