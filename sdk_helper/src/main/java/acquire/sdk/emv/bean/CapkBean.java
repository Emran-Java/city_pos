package acquire.sdk.emv.bean;

/**
 * Emv Capk bean information
 *
 * @author Janson
 * @date 2021/11/4 9:18
 */
public class CapkBean {
    private String modulus ;
    private String exponent;
    private String hash;
    private String expiredDate;
    private String rid;
    private String rfu;
    private int pkModulusLen;
    private int index;
    private int algorithmIndicator;
    private int hashAlgorithm;

    public String getModulus() {
        return modulus;
    }

    public void setModulus(String modulus) {
        this.modulus = modulus;
    }

    public String getExponent() {
        return exponent;
    }

    public void setExponent(String exponent) {
        this.exponent = exponent;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getRfu() {
        return rfu;
    }

    public void setRfu(String rfu) {
        this.rfu = rfu;
    }

    public int getPkModulusLen() {
        return pkModulusLen;
    }

    public void setPkModulusLen(int pkModulusLen) {
        this.pkModulusLen = pkModulusLen;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getAlgorithmIndicator() {
        return algorithmIndicator;
    }

    public void setAlgorithmIndicator(int algorithmIndicator) {
        this.algorithmIndicator = algorithmIndicator;
    }

    public int getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(int hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }
}
