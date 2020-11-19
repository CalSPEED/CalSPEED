package gov.ca.cpuc.calspeed.android;

import android.util.Log;

import java.util.Comparator;

/**
 * Created by joshuaahn on 8/8/16.
 */
class DisplayResultsComparator implements Comparator<DisplayResults> {

    DisplayResultsComparator() {
        super();
    }

    @Override
    public int compare(DisplayResults a, DisplayResults b) {
        String aName = a.getName();
        String bName = b.getName();

        if (aName.compareTo(bName) == 0) {
            int aDownload = a.getDownloadIndex();
            int bDownload = b.getDownloadIndex();
            if (aDownload < bDownload) {
                return 1;
            } else if(aDownload > bDownload) {
                return -1;
            } else {
                int aUpload = a.getUploadIndex();
                int bUpload = b.getUploadIndex();
                if (aUpload > bUpload) {
                    return 1;
                } else if (aUpload < bUpload) {
                    return -1;
                } else {
                    return a.getTechIndex().compareTo(b.getTechIndex());
                }
            }
        } else {
            Log.d(getClass().getSimpleName(), aName);
            Log.d(getClass().getSimpleName(), bName);
            return aName.compareTo(bName);
        }
    }


}
