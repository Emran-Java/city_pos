package acquire.core.fragment.card;

import acquire.base.activity.callback.FragmentCallback;

/**
 * A callback for {@link CardFragment}
 *
 * @author Janson
 * @date 2021/8/19 15:02
 */
public abstract class CardFragmentCallback implements FragmentCallback<Void> {

    public abstract void onManual();

} 
