package acquire.base.widget.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import acquire.base.databinding.BaseKeyboardPointNumberBinding;


/**
 * A number keyboard with point
 *
 * @author Janson
 * @date 2022/8/9 10:21
 */
public class NumberPointKeyboard extends BaseKeyboard {


    public NumberPointKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs,0);
        BaseKeyboardPointNumberBinding binding = BaseKeyboardPointNumberBinding.inflate(LayoutInflater.from(context),this, true);
        mapKey(binding.key0,K_0);
        mapKey(binding.key1,K_1);
        mapKey(binding.key2,K_2);
        mapKey(binding.key3,K_3);
        mapKey(binding.key4,K_4);
        mapKey(binding.key5,K_5);
        mapKey(binding.key6,K_6);
        mapKey(binding.key7,K_7);
        mapKey(binding.key8,K_8);
        mapKey(binding.key9,K_9);
        mapKey(binding.key00,K_00);
        mapKey(binding.keyPoint,K_POINT);
        mapKey(binding.keyBack,K_BACKSPACE);
        mapKey(binding.keyEnter,K_ENTER);
        mapKey(binding.keyCancel,K_CANCEL);
    }
}
