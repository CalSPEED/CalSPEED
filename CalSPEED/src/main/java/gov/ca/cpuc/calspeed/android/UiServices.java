/*
Original work: Copyright 2009 Google Inc. All Rights Reserved.

Modified work: The original source code comes from the NDT Android app
               that is available from http://code.google.com/p/ndt/.
               It's modified for the CalSPEED Android app by California
               State University Monterey Bay (CSUMB) on April 29, 2013.

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
 * Provides platform-specific UI services. Defines several functions for
 * platform-specific classes to implement, to help the working thread dispatch
 * messages to the UI.
 *
 * <p>
 * Flow of the Interface:
 * <ol>
 * <li>Calls onBeginTest() on test begins.
 * <li>Calls appendString() whenever new output exists, could be sent to
 * different views.
 * <li>Calls increaseProgress() when a work step has been finished.
 * <li>Calls onFailure() when test failed, to leave an interface for some
 * platform-specific debug methods or just output some failure messages.
 * <li>Calls submitData() to pass the result variable into the main activity for
 * later reference (in email app, etc.).
 * <li>Calls onEndTest() to finish the test.
 * </ol>
 */
public interface UiServices {
  /**
   * Id refers to the main view for sending the output string.
   *
   * @see #appendString
   */
  int MAIN_VIEW = 0;

  /**
   * Id refers to the statistics view for sending the output string.
   *
   * @see #appendString
   */
  int STAT_VIEW = 1;

  /**
   * Id refers to the diagnose view for sending the output string.
   *
   * @see #appendString
   */
  int DIAG_VIEW = 2;

  /**
   * Id refers to the debug view for sending the output string.
   *
   * @see #appendString
   */
  int DEBUG_VIEW = 3;
  /**
   * Id refers to the summary view for minimal tester output window.
   *
   * @see #appendString
   */
  int SUMMARY_VIEW = 4;


  /**
   * Sends message to the designated view.
   *
   * @param str the string which is sent to the statistics view
   * @param viewId id that indicates where the string should be sent to, could
   *        be {@link #MAIN_VIEW} or {@link #STAT_VIEW}
   */
  void appendString(String str, int viewId);

  /**
   * Called each time a test step completes. Should be called a maximum of
   * TEST_STEPS times during the whole test.
   */
  void incrementProgress(Integer increment);

  /**
   * Notifies the callee that the test is starting.
   */
  void onBeginTest();

  /**
   * Notifies the callee that the test has ended.
   */
  void onEndTest();

  /**
   * Notifies the callee to print Lat Long values.
   */
  void printLatLong();
  /**
   * Called when the test ends abnormally. Should be called before onEndTest();
   */
  void onFailure(String errorMessage);

  /**
   * Called when packet queuing is detected.
   */

  void onPacketQueuingDetected();

  /**
   * Called when the test 'login' packet has been successfully sent.
   */

  void onLoginSent();

  /**
   * Abstract to make the logging action generic in different platforms.
   *
   * @param str message which is sent to the log.
   */
  void logError(String str);

  /**
   * Updates the status message, which indicates what test is running.
   */
  void updateStatus(String status);

  /**
   * Updates the status panel. (Applet-specific)
   */

  void updateStatusPanel(String status);

  /**
   * Returns true if the test should be aborted, false if it should continue.
   */

  boolean wantToStop();

  // Hack for the Applet's JavaScript access API extension
  void setVariable(String name, int value);
  void setVariable(String name, double value);
  void setVariable(String name, Object value);



}


