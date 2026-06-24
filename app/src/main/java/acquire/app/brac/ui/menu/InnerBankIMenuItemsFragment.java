package acquire.app.brac.ui.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//import com.zztl.pos.ucb.databinding.AppFragmentBankMenuBinding;
import com.zztl.pos.ucb.databinding.AppFragmentInnerBankMenuBinding;

import acquire.app.fragment.main.menu.MainMenu;
import acquire.app.fragment.main.menu.MenuItem;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.settings.contents.MenuChangeListen;

/**
 * A menu fragment.
 *
 * @author Janson
 * @date 2021/8/6 14:53
 */
public class InnerBankIMenuItemsFragment extends BaseFragment {
    private List<MenuItem> menuItems;

    private MenuAdapter mainMenuAdapter;
    private AppFragmentInnerBankMenuBinding binding;

    private MenuFragmentTouchListener touchListener;

    public static InnerBankIMenuItemsFragment newInstance(List<MenuItem> menuItems) {
        InnerBankIMenuItemsFragment fragment = new InnerBankIMenuItemsFragment();
        fragment.menuItems = menuItems;
        return fragment;
    }

    public static InnerBankIMenuItemsFragment newInstance(List<MenuItem> menuItems, MenuFragmentTouchListener onTouchListener) {
        InnerBankIMenuItemsFragment fragment = new InnerBankIMenuItemsFragment();
        fragment.menuItems = menuItems;
        fragment.touchListener = onTouchListener;
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AppFragmentInnerBankMenuBinding.inflate(inflater, container, false);
        mainMenuAdapter = new MenuAdapter(mActivity, menuItems);

        binding.rvBankItemsMenu.setAdapter(mainMenuAdapter);

        MenuChangeListen.getInstance().getMemo().observe(getViewLifecycleOwner(), memo -> {
            if (memo) {
                mainMenuAdapter.notifyDataSetChanged();
            }
        });

        binding.rvBankItemsMenu.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    if (touchListener != null) {
                        touchListener.onFragmentBTouched();
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        return binding.getRoot();
    }

    public void clickByPhysicalKeyBoard(int position) {
        if (mainMenuAdapter != null) {
            RecyclerView.ViewHolder holder = binding.rvBankItemsMenu.findViewHolderForAdapterPosition(position);
            if (holder != null) {
                holder.itemView.setPressed(true);
                holder.itemView.performClick();
            }
        }
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }

    public interface MenuFragmentTouchListener {
        void onFragmentBTouched();
    }
}