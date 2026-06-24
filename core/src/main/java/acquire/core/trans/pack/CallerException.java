package acquire.core.trans.pack;

import acquire.core.constant.CallerResult;

/**
 * Communication exception
 *
 * @author Janson
 * @date 2022/9/13 15:05
 */
public class CallerException extends Exception{
    private final @CallerResult.CallerResultDef int callerResult;

    public CallerException(@CallerResult.CallerResultDef int callerResult, String message) {
        super(message);
        this.callerResult = callerResult;
    }

    public int getCallerResult() {
        return callerResult;
    }
}
