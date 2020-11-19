/*
Copyright (c) 2013, California State University Monterey Bay (CSUMB).
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

class Key {

    static Integer getUpBucketKey(String bandwidthAsString) {
		Double bandwidth;
		try {
			bandwidth = Double.parseDouble(bandwidthAsString);
		} catch (Exception e) {
			return 11;
		}
		if (bandwidth <= 0.0) {
			return 11;
		} else if (bandwidth > 0.0 && bandwidth < 0.2) {
			return 0;
		} else if (bandwidth >= 0.2 && bandwidth < 0.75) {
			return 1;
		} else if (bandwidth >= 0.75 && bandwidth < 1.5) {
			return 2;
		} else if (bandwidth >= 1.5 && bandwidth < 3) {
			return 3;
		} else if (bandwidth >= 3 && bandwidth < 6) {
			return 4;
		} else if (bandwidth >= 6 && bandwidth < 10) {
			return 5;
		} else if (bandwidth >= 10 && bandwidth < 25) {
			return 6;
		} else if (bandwidth >= 25 && bandwidth < 50) {
			return 7;
		} else if (bandwidth >= 50 && bandwidth < 100) {
			return 8;
		} else if (bandwidth >= 100 && bandwidth < 1000) {
			return 9;
		} else if (bandwidth >= 1000) {
			return 10;
		} else {
			return 11;
		}
	}
	
	static Integer getDownBucketKey(String bandwidthAsString) {
        Double bandwidth;
        try {
            bandwidth = Double.parseDouble(bandwidthAsString);
        } catch (Exception e) {
            return 23;
        }
		if (bandwidth <= 0.0) {
			return 23;
		} else if (bandwidth>= 0.0 && bandwidth < 0.2) {
            return 12;
        } else if (bandwidth >= 0.2 && bandwidth < 0.75) {
            return 13;
        } else if (bandwidth >= 0.75 && bandwidth < 1.5) {
            return 14;
        } else if (bandwidth >= 1.5 && bandwidth < 3) {
            return 15;
        } else if (bandwidth >= 3 && bandwidth < 6) {
            return 16;
        } else if (bandwidth >= 6 && bandwidth < 10) {
            return 17;
        } else if (bandwidth >= 10 && bandwidth < 25) {
            return 18;
        } else if (bandwidth >= 25 && bandwidth < 50) {
            return 19;
        } else if (bandwidth >= 50 && bandwidth < 100) {
            return 20;
        } else if (bandwidth >= 100 && bandwidth < 1000) {
            return 21;
        } else if (bandwidth >= 1000) {
            return 22;
        } else {
            return 23;
        }
	}
}
