package jn.mjz.web.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    //11:11
    public static String getFormattedTime(Long timeStamp){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date(timeStamp));
    }

    //2019-2-10
    public static  String getFormattedDate(Long timeStamp){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        return formatter.format(new Date(timeStamp));
    }
}
