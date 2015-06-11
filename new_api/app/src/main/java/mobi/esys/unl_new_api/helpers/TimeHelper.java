package mobi.esys.unl_new_api.helpers;

import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeHelper {
    public static String getCurrentTimeStamp() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddkkmmss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));
            Logger.d(dateFormat.format(new Date()));
            return dateFormat.format(new Date());
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }


}
