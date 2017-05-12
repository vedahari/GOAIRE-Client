package edu.vt.ece.onaire;

/**
 * Created by vedahari on 4/13/2017.
 */

public final class Constants {
    public final class Time{
        public static final long ONE_SECOND = 1000;
        public static final long ONE_MIN = 1000 * 60;
        public static final long TWO_MIN = ONE_MIN * 2;
        public static final long FIVE_MIN = ONE_MIN * 5;
        public static final long MEASURE_TIME = 1000 * 30;
        //public static final long POLLING_FREQ = 1000 * 10;
        public static final long POLLING_FREQ = ONE_SECOND;
    }
    public final class Accuracy{
        public static final float MIN_ACCURACY = 25.0f;
        public static final float MIN_LAST_READ_ACCURACY = 5.0f;
    }
    
    
    
}
