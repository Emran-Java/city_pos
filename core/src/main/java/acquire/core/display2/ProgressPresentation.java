package acquire.core.display2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import acquire.core.databinding.CorePresentationProgressBinding;

/**
 * A prompt presentation for dual screen
 *
 * @author Janson
 * @date 2022/8/29 13:51
 */
public class ProgressPresentation extends BasePresentation {

    public ProgressPresentation(Context outerContext) {
        super(outerContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CorePresentationProgressBinding binding = CorePresentationProgressBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
    }
}
