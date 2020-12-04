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

public class DisplayResults {

    private String name;
    private final int uploadIndex;                // Upload speed index
    private final int downloadIndex;              // Download speed index
    private String technologyCode;          // Name of the technology used
    private final String techIndex;               // Name of the technology code index

    DisplayResults(String name, int upIndex, int dnIndex, String techCode) {
        this.name = name;
        this.uploadIndex = upIndex;
        this.downloadIndex = dnIndex;
        this.techIndex = techCode;
        convertTechCodeDetail(techCode);
    }

    /* Setters */
    public void setName(String name) {
        this.name = name;
    }

    private void convertTechCodeDetail(String code) {
        this.technologyCode = TechnologyCode.techCodeDictionary.get(Integer.parseInt(code));
    }

    /* Getters */
    public String getName() {
        return name;
    }

    public int getUploadIndex() {
        return uploadIndex;
    }

    public int getDownloadIndex() {
        return downloadIndex;
    }

    public String getTechIndex() {
        return this.techIndex;
    }

    public String getTechName() {
        return technologyCode;
    }

}
