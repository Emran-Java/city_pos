package acquire.core.display2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import acquire.core.R;
import acquire.core.databinding.CorePresentationResultBinding;

/**
 * A result prompt presentation for dual screen
 *
 * @author Janson
 * @date 2022/10/18 11:00
 */
public class ResultPresentation extends BasePresentation {
    private final boolean result;
    private final String message;

    public ResultPresentation(Context outerContext, boolean result, String message) {
        super(outerContext);
        this.result = result;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CorePresentationResultBinding binding = CorePresentationResultBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        binding.tvContent.setText(message);
        if (result) {
            binding.lottieAnimation.setAnimation(R.raw.lottie_display2_success);
        } else {
            binding.lottieAnimation.setAnimation(R.raw.lottie_display2_failed);
        }
        binding.lottieAnimation.playAnimation();
    }

}
