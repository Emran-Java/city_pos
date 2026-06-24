package acquire.sdk.emv.listener;


import java.util.List;

import acquire.sdk.emv.bean.EmvReadyBean;

/**
 * A abstract EmvL3Listener
 *
 * @author Janson
 * @date 2021/4/15 16:11
 */
public interface EmvListener {

    /**
     * ready to read card
     *
     * @param emvReadyBean describe the card entry mode and status
     */
    void onReady(EmvReadyBean emvReadyBean, EmvResponser.SimpleResponser simpleResponser);

    /**
     * reading card
     */
    void onReading(EmvResponser.SimpleResponser simpleResponser);

    /**
     * select emv aid
     *
     * @param preferNames aids prefer name
     * @return aid index, If -1, failed.
     */
    void onSelectAid(List<String> preferNames, EmvResponser.IntegerResponser integerResponser);

    /**
     * Insert failed
     *
     * @return <code>true</code> if emv continues; <code>false</code> if cancel card
     */
    void onInsertError(EmvResponser.BooleanResponser integerResponse);

    /**
     * final select
     */
    void onFinalSelect(EmvResponser.SimpleResponser simpleResponser);

    /**
     * see phone
     *
     * @return true if retry card; false if cancelling card.
     */
    void onSeePhone(EmvResponser.BooleanResponser integerResponser);

    /**
     * get card number
     *
     * @param pan card number
     * @return true if emv continues; false if card cancel.
     */
    void onCardNum(String pan, EmvResponser.BooleanResponser integerResponser);

    /**
     * input PIN
     *
     * @param online      online PIN
     * @param pinTryCount Entered PIN times
     * @return PIN result
     */
    void onInputPin(boolean online, int pinTryCount, EmvResponser.PinResponser pinResponser);

    /**
     * emv result
     *
     * @param success   true if emv process executed success.
     * @param emvResult EMV result code.
     * @see acquire.sdk.emv.constant.EmvResult
     */
    void onResult(boolean success, int emvResult);

}
