package gov.ca.cpuc.calspeed.android;

import java.util.Comparator;

public class MetricComparator implements Comparator<Metric> {

    @Override
    public int compare(Metric a, Metric b) {
        if (a.getServer().equals("WEST") && !(b.getServer().equals("WEST"))) {
            return -1;
        } else if (a.getServer().equals("EAST") && !(b.getServer().equals("EAST"))) {
            return 1;
        } else {
            if (a.getDirection().equals("UP") && !(b.getDirection().equals("UP"))) {
                return -1;
            } else if (a.getDirection().equals("DOWN") && !(b.getDirection().equals("DOWN"))) {
                return 1;
            } else {
                Integer aInt = (int) Double.parseDouble(a.getTimeRange());
                Integer bInt = (int) Double.parseDouble(b.getTimeRange());
                if (aInt == bInt) {
                    return a.getThreadID() - b.getThreadID();
                } else {
                    return aInt - bInt;
                }
            }
        }
    }
}
