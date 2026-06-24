package acquire.core.model;

public class CardBinModel {

    private int cardNumberLength;
    private String startBin;
    private String endBin;
    private String cardTitle;

    public CardBinModel(int cardNumberLength, String startBin, String endBin, String cardTitle) {
        this.cardNumberLength = cardNumberLength;
        this.startBin = startBin;
        this.endBin = endBin;
        this.cardTitle = cardTitle;
    }

    public int getCardNumberLength() {
        return cardNumberLength;
    }

    public void setCardNumberLength(int cardNumberLength) {
        this.cardNumberLength = cardNumberLength;
    }

    public String getStartBin() {
        return startBin;
    }

    public void setStartBin(String startBin) {
        this.startBin = startBin;
    }

    public String getEndBin() {
        return endBin;
    }

    public void setEndBin(String endBin) {
        this.endBin = endBin;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }


}
