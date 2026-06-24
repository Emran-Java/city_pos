package acquire.core.fragment.card;

import acquire.core.bean.StepBean;
import acquire.core.trans.BaseStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * The arguments of {@link CardFragment}
 *
 * @author Janson
 * @date 2021/6/8 15:16
 */
public class CardFragmentArgs {
    /**
     * card suppor entry.
     * @see EntryMode
     */
    private final int supportEntry;
    private final BaseStep packStep;
    private final BaseStep pinStep;
    private final boolean forcePin;
    private final StepBean stepBean;
    private boolean accessibilityPin;
    /**
     * create a CardFragmentArgs
     * @param supportEntry card suppor entry. see {@link EntryMode}.
     * @param packStep online request step.
     * @param pinStep pin input step.
     * @param forcePin if true, force to input pin.
     * @param stepBean step chain arguments.
     */
    public CardFragmentArgs(int supportEntry, BaseStep packStep,BaseStep pinStep, boolean forcePin, StepBean stepBean,boolean accessibilityPin) {
        this.supportEntry = supportEntry;
        this.packStep = packStep;
        this.forcePin = forcePin;
        this.stepBean = stepBean;
        this.pinStep = pinStep;
        this.accessibilityPin = accessibilityPin;
    }
    public boolean isAccessibilityPin() {
        return accessibilityPin;
    }

    /**
     * get card suppor entry.
     * @see EntryMode
     */
    public int getSupportEntry() {
        return supportEntry;
    }

    public BaseStep getPackStep() {
        return packStep;
    }

    public BaseStep getPinStep() {
        return pinStep;
    }

    public boolean isForcePin() {
        return forcePin;
    }

    public StepBean getStepBean() {
        return stepBean;
    }
}
