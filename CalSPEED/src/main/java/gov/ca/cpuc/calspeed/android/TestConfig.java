
package gov.ca.cpuc.calspeed.android;


/**
 * Created by joshuaahn on 5/22/19.
 */


class TestConfig {
    String iperfPath;
    String serverIp;
    String port;
    Integer testTime;
    Integer testInterval;
    String format;

    TestConfig(String iperfPath, String serverIp, String port, String format, Integer testTime,
               Integer testInterval) {
        this.iperfPath = iperfPath;
        this.serverIp = serverIp;
        this.port = port;
        this.format = format;
        this.testTime = testTime;
        this.testInterval = testInterval;
    }

    TestConfig(String iperfPath, String serverIp, String port, Integer testTime) {
        this.iperfPath = iperfPath;
        this.serverIp = serverIp;
        this.port = port;
        this.format = Constants.DEFAULT_WINDOW_SIZE_FORMAT;
        this.testTime = testTime;
        this.testInterval = Constants.DEFAULT_TEST_INTERVAL;
    }

    TestConfig(String iperfPath, String serverIp, String port) {
        this.iperfPath = iperfPath;
        this.serverIp = serverIp;
        this.port = port;
        this.format = Constants.DEFAULT_WINDOW_SIZE_FORMAT;
        this.testTime = Constants.DEFAULT_TEST_TIME;
        this.testInterval = Constants.DEFAULT_TEST_INTERVAL;
    }

    String createIperfCommandLine() {
        return "";
    }

    String whichServer() {
        if (this.serverIp.equals(Globals.WEST_SERVER)) {
            return "WEST";
        } else if (this.serverIp.equals(Globals.EAST_SERVER)) {
            return "EAST";
        } else {
            return null;
        }
    }

    String getIp() {
        return this.serverIp;
    }

    String getPort() {
        return this.port;
    }

    int getTestTime() {
        return this.testTime;
    }

    void setIp(String ip) {
        this.serverIp = ip;
    }

    void setPort(String port) {
        this.port = port;
    }

    void setFormat(String format) {
        this.format = format;
    }

    void setFormat(Integer format) {
        this.format = String.format("$1%dk", format);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IP: ").append(this.serverIp);
        sb.append("\nPort: ").append(this.port);
        sb.append("\nTest time length: ").append(this.testTime);
        sb.append("\nTest time interveral: ").append(this.testInterval);
        sb.append("\nFormat: ").append(this.format);
        return sb.toString();
    }


}
