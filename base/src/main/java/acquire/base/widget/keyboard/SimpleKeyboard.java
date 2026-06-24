package acquire.base.widget.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import acquire.base.databinding.BaseKeyboardSimpleBinding;


/**
 * A number keyboard
 *
 * @author Janson
 * @date 2018/3/6
 */
public class SimpleKeyboard extends BaseKeyboard {


    public SimpleKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs,0);
        BaseKeyboardSimpleBinding binding = BaseKeyboardSimpleBinding.inflate(LayoutInflater.from(context),this, true);
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
        mapKey(binding.keyBack,K_BACKSPACE);
    }






}
