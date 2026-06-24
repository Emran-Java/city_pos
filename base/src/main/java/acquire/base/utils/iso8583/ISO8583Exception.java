package acquire.base.utils.iso8583;

/**
 * ISO8583 pack error
 *
 * @author Janson
 * @date 2021/9/26 11:11
 */
public class ISO8583Exception extends Exception {
    private static final long serialVersionUID = 1L;

    private String fieldTag;

    public ISO8583Exception(String strException) {
        super(strException);
    }

    public ISO8583Exception(String fieldTag,String strException) {
        super(strException);
        this.fieldTag = fieldTag;
    }

    public String getFieldTag() {
        return fieldTag;
    }
}
