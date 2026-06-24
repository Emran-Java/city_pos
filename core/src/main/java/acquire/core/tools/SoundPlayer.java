package acquire.core.tools;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import acquire.base.BaseApplication;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;

/**
 * Sound player
 *
 * @author Janson
 * @date 2021/10/25 11:18
 */
public class SoundPlayer {
    private final SparseIntArray soundAudios = new SparseIntArray();
    private final List<Integer> mediaAudios = new ArrayList<>();
    private SoundPool soundPool;
    private int lastAudio;
    private final Queue<Integer> audioQueue = new ConcurrentLinkedQueue<>();
    private static volatile SoundPlayer instance;
    private MediaPlayer mediaPlayer;

    private SoundPlayer() {
    }

    public static SoundPlayer getInstance() {
        if (instance == null) {
            synchronized (SoundPlayer.class) {
                if (instance == null) {
                    instance = new SoundPlayer();
                }
            }
        }
        return instance;
    }

    public void init() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        Context context = BaseApplication.getAppContext();
        soundAudios.put(R.raw.click_keyboard, soundPool.load(context, R.raw.click_keyboard, 1));
        soundAudios.put(R.raw.ding, soundPool.load(context, R.raw.ding, 1));

        //number
        soundAudios.put(R.raw.num_0, soundPool.load(context, R.raw.num_0, 1));
        soundAudios.put(R.raw.num_1, soundPool.load(context, R.raw.num_1, 1));
        soundAudios.put(R.raw.num_2, soundPool.load(context, R.raw.num_2, 1));
        soundAudios.put(R.raw.num_3, soundPool.load(context, R.raw.num_3, 1));
        soundAudios.put(R.raw.num_4, soundPool.load(context, R.raw.num_4, 1));
        soundAudios.put(R.raw.num_5, soundPool.load(context, R.raw.num_5, 1));
        soundAudios.put(R.raw.num_6, soundPool.load(context, R.raw.num_6, 1));
        soundAudios.put(R.raw.num_7, soundPool.load(context, R.raw.num_7, 1));
        soundAudios.put(R.raw.num_8, soundPool.load(context, R.raw.num_8, 1));
        soundAudios.put(R.raw.num_9, soundPool.load(context, R.raw.num_9, 1));
        soundAudios.put(R.raw.num_10, soundPool.load(context, R.raw.num_10, 1));
        soundAudios.put(R.raw.num_11, soundPool.load(context, R.raw.num_11, 1));
        soundAudios.put(R.raw.num_12, soundPool.load(context, R.raw.num_12, 1));
        soundAudios.put(R.raw.num_13, soundPool.load(context, R.raw.num_13, 1));
        soundAudios.put(R.raw.num_14, soundPool.load(context, R.raw.num_14, 1));
        soundAudios.put(R.raw.num_15, soundPool.load(context, R.raw.num_15, 1));
        soundAudios.put(R.raw.num_16, soundPool.load(context, R.raw.num_16, 1));
        soundAudios.put(R.raw.num_17, soundPool.load(context, R.raw.num_17, 1));
        soundAudios.put(R.raw.num_18, soundPool.load(context, R.raw.num_18, 1));
        soundAudios.put(R.raw.num_19, soundPool.load(context, R.raw.num_19, 1));
        soundAudios.put(R.raw.num_20, soundPool.load(context, R.raw.num_20, 1));
        soundAudios.put(R.raw.num_30, soundPool.load(context, R.raw.num_30, 1));
        soundAudios.put(R.raw.num_40, soundPool.load(context, R.raw.num_40, 1));
        soundAudios.put(R.raw.num_50, soundPool.load(context, R.raw.num_50, 1));
        soundAudios.put(R.raw.num_60, soundPool.load(context, R.raw.num_60, 1));
        soundAudios.put(R.raw.num_70, soundPool.load(context, R.raw.num_70, 1));
        soundAudios.put(R.raw.num_80, soundPool.load(context, R.raw.num_80, 1));
        soundAudios.put(R.raw.num_90, soundPool.load(context, R.raw.num_90, 1));
        soundAudios.put(R.raw.text_and, soundPool.load(context, R.raw.text_and, 1));
        soundAudios.put(R.raw.text_hundred, soundPool.load(context, R.raw.text_hundred, 1));
        soundAudios.put(R.raw.text_thousand, soundPool.load(context, R.raw.text_thousand, 1));
        soundAudios.put(R.raw.text_million, soundPool.load(context, R.raw.text_million, 1));
        soundAudios.put(R.raw.text_billion, soundPool.load(context, R.raw.text_billion, 1));
        soundAudios.put(R.raw.text_cent, soundPool.load(context, R.raw.text_cent, 1));
        soundAudios.put(R.raw.text_cents, soundPool.load(context, R.raw.text_cents, 1));
        soundAudios.put(R.raw.text_dollar, soundPool.load(context, R.raw.text_dollar, 1));
        soundAudios.put(R.raw.text_dollars, soundPool.load(context, R.raw.text_dollars, 1));
        soundAudios.put(R.raw.text_total_amount_is, soundPool.load(context, R.raw.text_total_amount_is, 1));
        soundAudios.put(R.raw.text_wait_for_card, soundPool.load(context, R.raw.text_wait_for_card, 1));
        soundAudios.put(R.raw.text_last_digit_cleared, soundPool.load(context, R.raw.text_last_digit_cleared, 1));
        soundAudios.put(R.raw.text_enter_button, soundPool.load(context, R.raw.text_enter_button, 1));
        soundAudios.put(R.raw.text_cancel_button, soundPool.load(context, R.raw.text_cancel_button, 1));
        soundAudios.put(R.raw.text_clear_button, soundPool.load(context, R.raw.text_clear_button, 1));

        soundAudios.put(R.raw.text_all_digits_entered, soundPool.load(context, R.raw.text_all_digits_entered, 1));
        soundAudios.put(R.raw.text_pin_timeout, soundPool.load(context, R.raw.text_pin_timeout, 1));
        soundAudios.put(R.raw.text_transaction_cancelled, soundPool.load(context, R.raw.text_transaction_cancelled, 1));
        soundAudios.put(R.raw.text_pin_cleared, soundPool.load(context, R.raw.text_pin_cleared, 1));
        //xxx digits entered, please continue with PIN or select the Enter button at the bottom right
        soundAudios.put(R.raw.text_continue_pin, soundPool.load(context, R.raw.text_continue_pin, 1));
        //xxx digits entered
        soundAudios.put(R.raw.text_digits_entered, soundPool.load(context, R.raw.text_digits_entered, 1));
        //Transaction Approved, please remove your card
        soundAudios.put(R.raw.text_transaction_success, soundPool.load(context, R.raw.text_transaction_success, 1));
        //Please remove your card
        soundAudios.put(R.raw.text_remove_card, soundPool.load(context, R.raw.text_remove_card, 1));


        mediaAudios.add(R.raw.text_pin_pad_below);
        /*
         *Please enter PIN. The layout is standard telephone layout with 1, 2, 3 at the top and Cancel,
         * Clear and Enter across the bottom. Slide your finger across the screen. Find the digits using the beeps,
         * then double tap anywhere on the screen to enter the digit.When finished find the Enter button and double tap to confirm.
         */
        mediaAudios.add(R.raw.text_pin_pad_start);
        /*
         * Please listen to these instructions or interrupt to pay. Insert your card at the bottom, tap it on the top of the screen, or swipe from left to right at the top.
         * The PIN Pad is on the bottom two-thirds of the screen, using a standard telephone layout. Numbers are not spoken, but the Cancel, Clear, and Enter buttons will announce their functions. Use beeps to locate digits. If you start above the PIN Pad, you'll hear "PIN Pad below."
         * Lift your finger and double tap anywhere to enter a digit.
         * To confirm, find Enter at the bottom right and double tap. Cancel (bottom left) cancels the transaction or clears all digits, while Clear (bottom middle) removes the last digit. Please tap, insert, or swipe your card to enter your PIN.
         */
        mediaAudios.add(R.raw.text_pin_guide);
    }

    private void playAudio(@RawRes int audioId) {
        audioQueue.offer(audioId);
        executeAudioQueue();
    }

    private void playAudios(@NonNull Queue<Integer> audioIds) {
        audioQueue.addAll(audioIds);
        executeAudioQueue();
    }

    private void executeAudioQueue() {
        if (soundPool == null) {
            init();
        }
        //stop last sound
        if (lastAudio > 0 && !mediaAudios.contains(lastAudio)) {
            soundPool.stop(lastAudio);
        }
        if (audioQueue.isEmpty()) {
            return;
        }
        Integer audioId = audioQueue.poll();
        if (audioId == null) {
            executeAudioQueue();
            return;
        }

        lastAudio = audioId;
        if (mediaAudios.contains(audioId)) {
            ThreadPool.postOnMain(() -> {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                mediaPlayer = MediaPlayer.create(BaseApplication.getAppContext(), audioId);
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    mediaPlayer = null;
                    executeAudioQueue();
                });
                mediaPlayer.start();
            });
        } else {
            if (mediaPlayer != null) {
                ThreadPool.postOnMain(() -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }
                });
            }
            int soundId = soundAudios.get(audioId);
            soundPool.play(soundId, 1, 1, 0, 0, 1);
            if (!audioQueue.isEmpty()) {
                ThreadPool.postDelayOnMain(this::executeAudioQueue, getRawDuration(audioId));
            }
        }

    }


    public void stop() {
        audioQueue.clear();
        if (!mediaAudios.contains(lastAudio)) {
            if (lastAudio > 0) {
                soundPool.stop(lastAudio);
            }
        } else {
            if (mediaPlayer != null) {
                ThreadPool.postOnMain(() -> {
                    mediaPlayer.stop();
                    mediaPlayer = null;
                });
            }
        }
    }

    public void playScan() {
        playAudio(R.raw.ding);
    }

    public void playClick() {
        playAudio(R.raw.click_keyboard);
    }

    public void playLastDigitClear() {
        playAudio(R.raw.text_last_digit_cleared);
    }

    public void playPinButtonEnter() {
        playAudio(R.raw.text_enter_button);
    }

    public void playPinButtonCancel() {
        playAudio(R.raw.text_cancel_button);
    }

    public void playPinClearButton() {
        playAudio(R.raw.text_clear_button);
    }

    public void playPinPadBelow() {
        if (lastAudio == R.raw.text_pin_pad_below && mediaPlayer != null) {
            return;
        }
        playAudio(R.raw.text_pin_pad_below);
    }

    public void playPinFinish() {
        playAudio(R.raw.text_all_digits_entered);
    }

    public void playPinTimeout() {
        playAudio(R.raw.text_pin_timeout);
    }

    public void playPinCancel() {
        playAudio(R.raw.text_transaction_cancelled);
    }

    public void playPinClear() {
        playAudio(R.raw.text_pin_cleared);
    }

    public void playRnibPadStart() {
        playAudio(R.raw.text_pin_pad_start);
    }


    public void playContinuePin(int hasDigits) {
        Queue<Integer> audios = new LinkedList<>(numberToAudio(hasDigits));
        audios.add(R.raw.text_continue_pin);
        playAudios(audios);
    }

    public void playPinEntered(int hasDigits) {
        Queue<Integer> audios = new LinkedList<>(numberToAudio(hasDigits));
        audios.add(R.raw.text_digits_entered);
        playAudios(audios);
    }
    public void playSuccess() {
        Queue<Integer> audios = new LinkedList<>();
        audios.add(R.raw.sound_success_bn);
        playAudios(audios);
    }

    public static void playSuccess(Context context) {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.sound_success_bn);
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playTransactionSuccess() {
        playAudio(R.raw.text_transaction_success);
    }


    public void playAmount(long amount) {
        Queue<Integer> audios = new LinkedList<>();
        if (amount >= 0) {
            int integerPart = (int) (amount / 100);
            int decimalPart = (int) (amount % 100);
            audios.add(R.raw.text_total_amount_is);
            if (integerPart > 0) {
                audios.addAll(numberToAudio(integerPart));
                if (integerPart > 1) {
                    audios.add(R.raw.text_dollars);
                } else {
                    audios.add(R.raw.text_dollar);
                }
            }
            if (decimalPart > 0) {
                if (integerPart > 0) {
                    audios.add(R.raw.text_and);
                }
                audios.addAll(numberToAudio(decimalPart));
                if (decimalPart > 1) {
                    audios.add(R.raw.text_cents);
                } else {
                    audios.add(R.raw.text_cent);
                }
            }
        }
        audios.add(R.raw.text_wait_for_card);
        audios.add(R.raw.text_pin_guide);
        playAudios(audios);
    }


    private List<Integer> numberToAudio(int number) {
        List<Integer> audios = new ArrayList<>();
        int[] belowTwenty = {0,
                R.raw.num_1,
                R.raw.num_2,
                R.raw.num_3,
                R.raw.num_4,
                R.raw.num_5,
                R.raw.num_6,
                R.raw.num_7,
                R.raw.num_8,
                R.raw.num_9,
                R.raw.num_10,
                R.raw.num_11,
                R.raw.num_12,
                R.raw.num_13,
                R.raw.num_14,
                R.raw.num_15,
                R.raw.num_16,
                R.raw.num_17,
                R.raw.num_18,
                R.raw.num_19};
        int[] tens = {0, 0,
                R.raw.num_20,
                R.raw.num_30,
                R.raw.num_40,
                R.raw.num_50,
                R.raw.num_60,
                R.raw.num_70,
                R.raw.num_80,
                R.raw.num_90};

        if (number >= 1_000_000_000) {
            audios.addAll(numberToAudio(number / 1_000_000_000));
            audios.add(R.raw.text_billion);
            if (number % 1_000_000_000 > 0) {
                audios.addAll(numberToAudio(number % 1_000_000_000));
            }
        } else if (number >= 1_000_000) {
            audios.addAll(numberToAudio(number / 1_000_000));
            audios.add(R.raw.text_million);
            if (number % 1_000_000 > 0) {
                audios.addAll(numberToAudio(number % 1_000_000));
            }
        } else if (number >= 1000) {
            audios.addAll(numberToAudio(number / 1000));
            audios.add(R.raw.text_thousand);
            if (number % 1000 > 0) {
                audios.addAll(numberToAudio(number % 1000));
            }
        } else if (number >= 100) {
            audios.addAll(numberToAudio(number / 100));
            audios.add(R.raw.text_hundred);
            if (number % 100 > 0) {
                audios.addAll(numberToAudio(number % 100));
            }
        } else if (number >= 20) {
            audios.add(tens[number / 10]);
            if (number % 10 > 0) {
                audios.add(belowTwenty[number % 10]);
            }
        } else if (number > 0) {
            audios.add(belowTwenty[number]);
        }

        return audios;
    }


    private int getRawDuration(int audioId) {
        if (audioId == R.raw.text_total_amount_is || audioId == R.raw.text_wait_for_card) {
            return 1200;
        }
        if (audioId == R.raw.text_dollar || audioId == R.raw.text_dollars || audioId == R.raw.text_cent || audioId == R.raw.text_cents) {
            return 800;
        }
        if (audioId == R.raw.num_70 || audioId == R.raw.num_17 || audioId == R.raw.num_16 || audioId == R.raw.num_60 || audioId == R.raw.text_thousand) {
            return 800;
        }
        if (audioId == R.raw.text_million || audioId == R.raw.text_billion || audioId == R.raw.text_and) {
            return 700;
        }
        if (audioId == R.raw.num_3
                || audioId == R.raw.num_5
                || audioId == R.raw.num_6
                || audioId == R.raw.num_7
                || audioId == R.raw.num_11
                || audioId == R.raw.num_12
                || audioId == R.raw.num_13
                || audioId == R.raw.num_14
                || audioId == R.raw.num_15
                || audioId == R.raw.num_18
                || audioId == R.raw.num_19
                || audioId == R.raw.num_20
                || audioId == R.raw.num_30
                || audioId == R.raw.num_40
                || audioId == R.raw.num_50
                || audioId == R.raw.num_80
                || audioId == R.raw.num_90
                || audioId == R.raw.text_hundred) {
            return 600;
        }
        if (audioId == R.raw.num_1
                || audioId == R.raw.num_2
                || audioId == R.raw.num_4
                || audioId == R.raw.num_8
                || audioId == R.raw.num_9
                || audioId == R.raw.num_10) {
            return 406;
        }
        return 0;
    }
}
