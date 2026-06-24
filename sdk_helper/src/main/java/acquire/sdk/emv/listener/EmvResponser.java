package acquire.sdk.emv.listener;

import acquire.sdk.emv.bean.PinResult;

/**
 * Emv Responser
 *
 * @author Janson
 * @date 2023/9/20 9:54
 */
public class EmvResponser {
    public interface SimpleResponser {
        void finish();
    }

    public interface BooleanResponser {
        void finish(boolean result);
    }

    public interface IntegerResponser {
        void finish(int result);
    }

    public interface PinResponser {
        void finish(PinResult pinResult);
    }
} 
