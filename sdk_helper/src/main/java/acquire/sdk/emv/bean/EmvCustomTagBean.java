package acquire.sdk.emv.bean;

import com.newland.sdk.emvl3.api.common.configuration.EMVTagAttr;

/**
 * Pack EMV Custom tag data
 *
 * @author Janson
 * @date 2023/8/8 14:43
 */
public class EmvCustomTagBean {
    private int tag;
    /**
     * Tag Template1. see  {@linkplain EMVTagAttr.Template}
     */
    private int template1;
    /**
     * Tag Template2.see  {@linkplain EMVTagAttr.Template}
     */
    private int template2;
    /**
     * Tag source.see  {@linkplain EMVTagAttr.Source}
     */
    private int source;
    /**
     * Tag format.{@linkplain EMVTagAttr.Format}
     */
    private int format;
    /**
     * Tag Min Length.
     */
    private int minLen;
    /**
     * Tag Max Length.
     */
    private int maxLen;

    public void setTag(int tag) {
        this.tag = tag;
    }

    public void setTemplate1(int template1) {
        this.template1 = template1;
    }

    public void setTemplate2(int template2) {
        this.template2 = template2;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public void setMinLen(int minLen) {
        this.minLen = minLen;
    }

    public void setMaxLen(int maxLen) {
        this.maxLen = maxLen;
    }

    public byte[] toByteArray() {
        EMVTagAttr emvTagAttr = new EMVTagAttr();
        emvTagAttr.setTag(tag);
        emvTagAttr.setTemplate1(template1);
        emvTagAttr.setTemplate2(template2);
        emvTagAttr.setSource(source);
        emvTagAttr.setFormat(format);
        emvTagAttr.setMinLen(minLen);
        emvTagAttr.setMaxLen(maxLen);
        return emvTagAttr.toByteArray();
    }
}
