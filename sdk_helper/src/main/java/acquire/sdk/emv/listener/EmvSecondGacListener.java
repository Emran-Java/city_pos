package acquire.sdk.emv.listener;

/**
 * Emv complete listener
 *
 * @author Janson
 * @date 2021/4/20 10:19
 */
public interface EmvSecondGacListener {
    /**
     * Second gac result
     *
     * @param result    true if success.
     */
    void completeResult(boolean result);

    void recard();
} 
