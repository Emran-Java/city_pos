package acquire.core.constant;

/**
 * Transaction result code
 *
 * @author Janson
 * @date 2018/3/29
 */
public class ResultCode {
    //------------Not from server-----------
    /**
     * fail
     */
    public static final String FL = "FL";
    /**
     * user cancel
     */
    public static final String UC = "UC";

    //------------may be from server-----------
    /**
     * 00 success
     */
    public static final String OK = "00";



    /**
     * Whether to be a custom response code (i.e. not from server)
     *
     * @author Janson
     * @date 2020/8/27 12:41
     */
    public static boolean isCustomCode(String code){
        switch (code){
            case UC:
            case FL:
                return true;
            default:
                return false;
        }
    }

}
