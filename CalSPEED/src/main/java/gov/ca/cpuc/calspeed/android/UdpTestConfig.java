
package gov.ca.cpuc.calspeed.android;


/**
 * Created by joshuaahn on 6/7/19.
 */


public class UdpTestConfig extends TestConfig {
    private Integer length;
    private String bandwidth;
    private Integer threadNumber;

    UdpTestConfig(String iperfPath, String serverIp, String port, String windowSizeFormat,
                  Integer length, String bandwidth, Integer testTime,
                  Integer testInterval) {
        super(iperfPath, serverIp, port, windowSizeFormat, testTime, testInterval);
        this.length = length;
        this.bandwidth = bandwidth;
    }

    UdpTestConfig(String iperfPath, String serverIp, String port,
                  Integer length, String bandwidth, Integer testTime) {
        super(iperfPath, serverIp, port, testTime);
        this.length = length;
        this.bandwidth = bandwidth;
    }

    UdpTestConfig(String iperfPath, String serverIp, String port,
                  Integer length, String bandwidth) {
        super(iperfPath, serverIp, port);
        this.length = length;
        this.bandwidth = bandwidth;
    }

    String createIperfCommandLine() {
        String iperf = this.iperfPath + Constants.IPERF_VERSION;
        return String.format(Constants.UDP_COMMAND, this.serverIp, this.port, iperf,
                this.length, this.bandwidth, this.format, this.testTime, this.testInterval);
    }

    String getBandwidth() {
        return this.bandwidth;
    }

    Integer getLength() {
        return this.length;
    }

    void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
    }

    void setLength(Integer newLength) {
        this.length = newLength;
    }

    void setBandwidth(Integer bandwidth) {
        this.bandwidth = String.format("$1%dk", bandwidth);
    }

    void setThreadNumber(Integer threadNum) {
        this.threadNumber = threadNum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IP: ").append(this.serverIp);
        sb.append("\nPort: ").append(this.port);
        sb.append("\nLength: ").append(this.length);
        sb.append("\nBandidth: ").append(this.bandwidth);
        String fullFormatName;
        switch (this.format) {
            case "k":
                fullFormatName = "Kilobits";
                break;
            case "K":
                fullFormatName = "Kilobytes";
                break;
            case "m":
                fullFormatName = "Megabits";
                break;
            case "M":
                fullFormatName = "Megabytes";
                break;
            default:
                fullFormatName = "Kilobits";
                break;
        }
        sb.append("\nFormat: ").append(fullFormatName);
        sb.append("\nTest time length: ").append(this.testTime);
        sb.append("\nTest time interval: ").append(this.testInterval);
        return sb.toString();
    }

}
