/*
Copyright (c) 2020, California State University Monterey Bay (CSUMB).
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. Neither the name of the CPUC, CSU Monterey Bay, nor the names of
       its contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
