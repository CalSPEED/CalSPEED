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
