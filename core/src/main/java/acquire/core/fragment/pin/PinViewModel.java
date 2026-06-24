package acquire.core.fragment.pin;

import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.TimerHandler;
import acquire.base.utils.ViewUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.tools.PinpadHelper;
import acquire.core.tools.SoundPlayer;
import acquire.sdk.device.BDevice;
import acquire.sdk.pin.constant.PinKeyCode;
import acquire.sdk.pin.listener.PinpadListener;
import acquire.sdk.pin.listener.RnibPinpadListener;
import acquire.sdk.sound.BBeeper;

/**
 * PIN view model
 *
 * @author Janson
 * @date 2022/8/26 16:11
 */
public class PinViewModel extends ViewModel {

    private final MutableLiveData<byte[]> pinBlock = new MutableLiveData<>();
    private final MutableLiveData<Integer> inputLength = new MutableLiveData<>();
    private final MutableLiveData<PinError> error = new MutableLiveData<>();
    private boolean isFinish;
    private PinpadHelper pinPad;
    private PinFragmentArgs args;

    public LiveData<byte[]> getPinBlock() {
        return pinBlock;
    }

    public LiveData<Integer> getInputLength() {
        return inputLength;
    }

    public LiveData<PinError> getError() {
        return error;
    }

    private final TimerHandler timerHandler = new TimerHandler();

    private void observePress() {
        timerHandler.startTimeout(10 * 1000, () -> {
            if (!isFinish) {
                Integer hasInputLen = inputLength.getValue();
                if (hasInputLen == null) {
                    hasInputLen = 0;
                }
                if (hasInputLen > 0) {
                    SoundPlayer.getInstance().playContinuePin(hasInputLen);
                }
                observePress();
            }
        });
    }

    public void init(PinFragmentArgs args) {
        this.args = args;
        pinPad = new PinpadHelper(args.getMasterIndex(), args.getAlgorithm());
    }

    public void setRandomKeyboard(List<View> numberKeyViews, List<View> funcKeyViews) {
        Map<String, int[]> buttons = packButtons(numberKeyViews, funcKeyViews);
        //set button to PIN pad
        if (args.isAccessPinPad()) {
            //blind RNIB mode
            pinPad.setRnibPinpadLayout(buttons);
        } else {
            //normal mode
            byte[] randomKeyCodes = pinPad.setPinpadLayout(buttons, true);
            if (randomKeyCodes == null) {
                //generate random keyboard failed
                LoggerUtils.e("Random pinpad generation failed.");
                error.postValue(new PinError(PinError.FAIL, BaseApplication.getAppString(R.string.core_pin_random_error)));
                return;
            }
            //Set PIN pad layouts bytes to keyboard number button
            for (int i = 0; i < randomKeyCodes.length; i++) {
                String codeText = Character.toString((char) randomKeyCodes[i]);
                ((TextView) numberKeyViews.get(i)).setText(codeText);
            }
        }
    }

    public void startPin() {
        isFinish = false;
        //input PIN
        ThreadPool.execute(() -> {
            boolean rnibPinPad = args.isAccessPinPad() && !BDevice.supportPhysicalKeyboard();
            boolean ttsSpeak = args.isAccessPinPad();
            if (ttsSpeak) {
                SoundPlayer.getInstance().playRnibPadStart();
                observePress();
            }
            if (rnibPinPad) {
                //blind RNIB mode
                pinPad.startRnibPinInput(args.isOnlinePin(), args.getPan(), args.getSupportPinLengths(), new RnibPinpadListener() {
                    @Override
                    public void onSlidNumberKey() {
                        BBeeper.beep(1000, 300);
                        timerHandler.resetTimeout();
                    }

                    @Override
                    public void onSlidNoDigitKey() {
                        timerHandler.resetTimeout();
                    }

                    @Override
                    public void onSlidBackSpace() {
                        SoundPlayer.getInstance().playPinClearButton();
                        timerHandler.resetTimeout();
                    }

                    @Override
                    public void onSlidEnter() {
                        SoundPlayer.getInstance().playPinButtonEnter();
                        timerHandler.resetTimeout();
                    }

                    @Override
                    public void onSlidCancel() {
                        SoundPlayer.getInstance().playPinButtonCancel();
                        timerHandler.resetTimeout();
                    }

                    @Override
                    public void onSlidUp() {
                        SoundPlayer.getInstance().playPinPadBelow();
                        timerHandler.resetTimeout();
                    }

                    @Override
                    public void onSlidDown() {
                        SoundPlayer.getInstance().playPinPadBelow();
                        timerHandler.resetTimeout();
                    }

                    @Override
                    public void onSlidLeft() {
                        SoundPlayer.getInstance().playPinPadBelow();
                        timerHandler.resetTimeout();
                    }

                    @Override
                    public void onSlidRight() {
                        SoundPlayer.getInstance().playPinPadBelow();
                        timerHandler.resetTimeout();
                    }

                    @Override
                    public void onCancel() {
                        SoundPlayer.getInstance().playPinCancel();
                        isFinish = true;
                        error.postValue(new PinError(PinError.CANCEL, BaseApplication.getAppString(R.string.core_pin_cancel)));
                    }

                    @Override
                    public void onError(int errorCode, String errorDescription) {
                        isFinish = true;
                        error.postValue(new PinError(PinError.FAIL, BaseApplication.getAppString(R.string.core_pin_error_format, errorCode, errorDescription)));
                    }

                    @Override
                    public void onTimeout() {
                        isFinish = true;
                        SoundPlayer.getInstance().playPinTimeout();
                        error.postValue(new PinError(PinError.FAIL, BaseApplication.getAppString(R.string.core_pin_timeout)));
                    }

                    @Override
                    public void onKeyDown(int len) {
                        ViewUtils.isFastClick(0);
                        int oldLen = inputLength.getValue() == null ? 0 : inputLength.getValue();
                        if (len == 0) {
                            SoundPlayer.getInstance().playPinClear();
                        } else if (oldLen > len) {
                            SoundPlayer.getInstance().playLastDigitClear();
                        } else {
                            SoundPlayer.getInstance().playPinEntered(len);
                        }
                        timerHandler.resetTimeout();
                        inputLength.postValue(len);

                    }

                    @Override
                    public void onPinRslt(byte[] pin) {
                        SoundPlayer.getInstance().playPinFinish();
                        isFinish = true;
                        pinBlock.postValue(pin);
                    }
                });
            } else {
                pinPad.startPinInput(args.isOnlinePin(), args.getPan(), args.getSupportPinLengths(), new PinpadListener() {
                    @Override
                    public void onCancel() {
                        isFinish = true;
                        if (!BDevice.supportPhysicalKeyboard()) {
                            SoundPlayer.getInstance().playClick();
                        }
                        error.postValue(new PinError(PinError.CANCEL, BaseApplication.getAppString(R.string.core_pin_cancel)));
                    }

                    @Override
                    public void onError(int errorCode, String errorDescription) {
                        isFinish = true;
                        error.postValue(new PinError(PinError.FAIL, BaseApplication.getAppString(R.string.core_pin_error_format, errorCode, errorDescription)));
                    }

                    @Override
                    public void onTimeout() {
                        if (ttsSpeak) {
                            SoundPlayer.getInstance().playPinTimeout();
                        }
                        isFinish = true;
                        error.postValue(new PinError(PinError.FAIL, BaseApplication.getAppString(R.string.core_pin_timeout)));
                    }

                    @Override
                    public void onKeyDown(int len) {
                        if (!BDevice.supportPhysicalKeyboard()) {
                            SoundPlayer.getInstance().playClick();
                        }
                        if (ttsSpeak) {
                            if (len == 0) {
                                SoundPlayer.getInstance().playPinClear();
                            } else {
                                SoundPlayer.getInstance().playPinEntered(len);
                            }
                            timerHandler.resetTimeout();
                        }
                        inputLength.postValue(len);
                    }

                    @Override
                    public void onPinRslt(byte[] pin) {
                        isFinish = true;
                        pinBlock.postValue(pin);
                    }
                });
            }
        });
    }

    public void cancelPin() {
        if (!isFinish) {
            LoggerUtils.e("invoke cancel PIN");
            pinPad.cancelPinInput();
            isFinish = true;
        }
    }

    private Map<String, int[]> packButtons(List<View> numberKeyViews, List<View> funcKeyViews) {
        Map<String, int[]> buttons = new HashMap<>();

        for (int i = 0; i < numberKeyViews.size(); i++) {
            String keyCode = "";
            switch (i) {
                case 0:
                    keyCode = PinKeyCode.K_0;
                    break;
                case 1:
                    keyCode = PinKeyCode.K_1;
                    break;
                case 2:
                    keyCode = PinKeyCode.K_2;
                    break;
                case 3:
                    keyCode = PinKeyCode.K_3;
                    break;
                case 4:
                    keyCode = PinKeyCode.K_4;
                    break;
                case 5:
                    keyCode = PinKeyCode.K_5;
                    break;
                case 6:
                    keyCode = PinKeyCode.K_6;
                    break;
                case 7:
                    keyCode = PinKeyCode.K_7;
                    break;
                case 8:
                    keyCode = PinKeyCode.K_8;
                    break;
                case 9:
                    keyCode = PinKeyCode.K_9;
                    break;
                default:
                    break;
            }
            View view = numberKeyViews.get(i);
            int[] coord = getCoords(view);
            buttons.put(keyCode, coord);
        }

        for (int i = 0; i < funcKeyViews.size(); i++) {
            String keyCode = "";
            switch (i) {
                case 0:
                    keyCode = PinKeyCode.K_BACKSPACE;
                    break;
                case 1:
                    keyCode = PinKeyCode.K_CANCEL;
                    break;
                case 2:
                    keyCode = PinKeyCode.K_ENTER;
                    break;
                default:
                    break;
            }
            View view = funcKeyViews.get(i);
            int[] coord = getCoords(view);
            buttons.put(keyCode, coord);
        }
        return buttons;
    }

    /**
     * get the view coordinates. Every element is int[4]{leftTopX,leftTopY,rigthBottomX,rightBottomY}
     */
    private int[] getCoords(View view) {
        int[] local = new int[2];
        view.getLocationOnScreen(local);
        int leftTopX = local[0];
        int leftTopY = local[1];
        int rightBottomX = local[0] + view.getWidth();
        int rightBottomY = local[1] + view.getHeight();
        int[] coords = new int[4];
        coords[0] = leftTopX;
        coords[1] = leftTopY;
        coords[2] = rightBottomX;
        coords[3] = rightBottomY;
        return coords;
    }

    public static class PinError {
        public final static int FAIL = -1, CANCEL = -2;
        private final int errorCode;
        private final String description;

        public PinError(int error, String description) {
            this.errorCode = error;
            this.description = description;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getDescription() {
            return description;
        }
    }
}
