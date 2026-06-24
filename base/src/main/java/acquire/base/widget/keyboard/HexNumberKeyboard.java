package acquire.base.widget.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import acquire.base.databinding.BaseKeyboardHexNumberBinding;


/**
 * A number keyboard
 *
 * @author Janson
 * @date 2018/3/6
 */
public class HexNumberKeyboard extends BaseKeyboard {


    public HexNumberKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs,0);
        BaseKeyboardHexNumberBinding binding = BaseKeyboardHexNumberBinding.inflate(LayoutInflater.from(context),this, true);
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
        mapKey(binding.keyA,K_A);
        mapKey(binding.keyB,K_B);
        mapKey(binding.keyC,K_C);
        mapKey(binding.keyD,K_D);
        mapKey(binding.keyE,K_E);
        mapKey(binding.keyF,K_F);
        mapKey(binding.keyBack,K_BACKSPACE);
        mapKey(binding.keyEnter,K_ENTER);
        mapKey(binding.keyCancel,K_CANCEL);
    }
}
