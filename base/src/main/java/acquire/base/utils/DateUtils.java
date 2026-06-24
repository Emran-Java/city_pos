package acquire.base.utils;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Date utils
 *
 * @author Janson
 * @date 2024/12/18 16:11
 */
public class DateUtils {
    public final static String YYYY = "yyyy";
    public final static String MMM = "MMM";
    public final static String YYYY_MM_DD = "yyyy/MM/dd";
    public final static String DD_MM_YYYY = "dd/MM/yyyy";
    public final static String HH_MM_SS = "HH:mm:ss";
    public final static String HH_MM = "HH:mm";
    public final static String YYYY_MM_DD_HH_MM_SS = "yyyy/MM/dd HH:mm:ss";
    public final static String MM_DD = "MM.dd";

    public final static String MMM_DD_YYYY = "MMM dd,yyyy";
    public final static String MMM_YYYY = "MMM yyyy";
    public final static String MMMM_DD = "MMMM dd";
    public final static String YYMMDD = "yyMMdd";
    public final static String YYYYMMDD = "yyyyMMdd";
    public final static String HHMM = "HHmm";
    public final static String HHMMSS = "HHmmss";
    public final static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";


    /**
     * format data&time
     * <pre>
     *     String resultDateTime = DateUtils.formatTime("20210327120320");
     *     //==> resultDateTime: 2021/03/27 12:03：20
     * </pre>
     *
     * @param dateTime the date&time(yyyyMMddHHmmss) value
     * @return return "" if failed; else, return formatted time.such as 2015/06/08 22:12:09.
     */
    public static String formatTime(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        return DateUtils.formatTime(dateTime, YYYYMMDDHHMMSS, YYYY_MM_DD_HH_MM_SS);
    }


    /**
     * format time
     * <pre>
     *     String resultDateTime = DateUtils.formatTime("120320");
     *     //==> resultDateTime: 12:03：20
     * </pre>
     *
     * @param dateTime the time(HHmmss) value
     * @return return "" if failed; else, return formatted time.such as 22:12:09.
     */
    public static String formatOnlyTime(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        return DateUtils.formatTime(dateTime, HHMMSS, HH_MM_SS);
    }

    /**
     * format data
     * <pre>
     *     String resultDate = DateUtils.formatTime("20210327");
     *     //==> resultDateTime: 2021/03/27
     * </pre>
     *
     * @param dateTime the date(yyyyMMdd) value
     * @return return "" if failed; else, return formatted time.such as 2015/06/08
     */
    public static String formatOnlyDate(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        return DateUtils.formatTime(dateTime, YYYYMMDD, DD_MM_YYYY);
    }
    public static String formatExpDate(String expDate) {
        if (expDate == null) {
            return null;
        }
        return DateUtils.formatTime(expDate, "yyMM", "MM/yy");
    }

    /**
     * format data&time.
     * <pre>
     *     String resultDateTime = DateUtils.formatTime("20210327","yyyyMMdd","yyyy/MM/dd");
     *     //==> resultDateTime: 2021/03/27
     * </pre>
     *
     * @param dateTime  the date&time value
     * @param inFormat  dateTime format. such as yyyyMMddHHmmss, yyyyMMdd, HHmmss
     * @param outFormat out date time format. such as yyyy/MM/dd HH:mm:ss, yyyy/MM/dd, HH:mm:ss
     * @return return "" if failed; else, return formatted time.such as 2015/06/08 22:12:09.
     */
    public static String formatTime(String dateTime, String inFormat, String outFormat) {
        if (dateTime == null) {
            return null;
        }
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat(inFormat, Locale.getDefault());
            Date date2 = parseFormat.parse(dateTime);
            if (date2 == null) {
                return "";
            }
            SimpleDateFormat resultFormat = new SimpleDateFormat(outFormat, Locale.getDefault());
            return resultFormat.format(date2);
        } catch (ParseException e) {
            LoggerUtils.e("formatTime " + dateTime + " failed!", e);
            return "";
        }
    }

    /**
     * format timestamp
     *
     * @param timeStamp timesatmp
     * @return return formatted time.such as 2015/06/08 22:12:09.
     */
    public static String formatTimeStamp(long timeStamp) {
        return formatTimeStamp(timeStamp, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * format timestamp
     *
     * @param timeStamp timesatmp
     * @param outFormat out date time format. such as yyyy/MM/dd HH:mm:ss, yyyy/MM/dd, HH:mm:ss
     * @return return formatted time.such as 2015/06/08 22:12:09.
     */
    public static String formatTimeStamp(long timeStamp, String outFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(outFormat, Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(timeStamp));
    }

    /**
     * format data.
     * <pre>
     *     String resultDateTime = DateUtils.formatTime(date);
     *     //==> resultDateTime: 2021/03/27 12:02:03
     * </pre>
     *
     * @param date date value
     * @return return "" if failed; else, return formatted time.such as 2015/06/08 22:12:09.
     */
    public static String formatTime(Date date) {
        return formatTime(date, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * format data.
     * <pre>
     *     String resultDateTime = DateUtils.formatTime(date,"yyyy/MM/dd HH:mm:ss");
     *     //==> resultDateTime: 2021/03/27 12:02:03
     * </pre>
     *
     * @param date      date value
     * @param outFormat out date time format. such as yyyy/MM/dd HH:mm:ss
     * @return return "" if failed; else, return formatted time.such as 2015/06/08 22:12:09.
     */
    public static String formatTime(Date date, String outFormat) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(outFormat, Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }


    public static Date getDate(String dateTime, String inFormat) {
        if (TextUtils.isEmpty(dateTime)) {
            return null;
        }
        SimpleDateFormat parseFormat = new SimpleDateFormat(inFormat, Locale.US);
        parseFormat.setTimeZone(TimeZone.getDefault());
        try {
            return parseFormat.parse(dateTime);
        } catch (ParseException e) {
            LoggerUtils.e("get date failed with " + dateTime + " by format " + inFormat, e);
            return null;
        }
    }

    public static String formatTimeISO8601(String dateTime) {
        return formatTime(dateTime, "yyyyMMddHHmmss", "yyyy-MM-dd'T'HH:mm:ss'Z'");
    }
} 
