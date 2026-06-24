package acquire.core.display2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import acquire.core.databinding.CorePresentationCardReadingBinding;

/**
 * A card reading status presentation for dual screen
 *
 * @author Janson
 * @date 2022/10/18 11:00
 */
public class CardReadingPresentation extends BasePresentation {
    public CardReadingPresentation(Context outerContext) {
        super(outerContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CorePresentationCardReadingBinding binding = CorePresentationCardReadingBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
    }
}
