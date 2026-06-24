package acquire.app.fragment.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.zztl.pos.city.databinding.AppFragmentMenuBinding;
import acquire.app.fragment.main.menu.MainMenuAdapter;
import acquire.app.fragment.main.menu.MenuItem;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;

/**
 * A menu fragment.
 *
 * @author Janson
 * @date 2021/8/6 14:53
 */
public class MenuFragment extends BaseFragment {
    private List<MenuItem> menuItems;

    private MainMenuAdapter mainMenuAdapter;
    private AppFragmentMenuBinding binding;

    private MenuFragmentTouchListener touchListener;

    public static MenuFragment newInstance(List<MenuItem> menuItems) {
        MenuFragment fragment = new MenuFragment();
        fragment.menuItems = menuItems;
        return fragment;
    }

    public static MenuFragment newInstance(List<MenuItem> menuItems, MenuFragmentTouchListener onTouchListener) {
        MenuFragment fragment = new MenuFragment();
        fragment.menuItems = menuItems;
        fragment.touchListener = onTouchListener;
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AppFragmentMenuBinding.inflate(inflater, container, false);
        mainMenuAdapter= new MainMenuAdapter(mActivity,menuItems);
        binding.rvMenu.setAdapter(mainMenuAdapter);
        binding.rvMenu.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if(e.getAction() == MotionEvent.ACTION_DOWN){
                    if(touchListener!=null){
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

    public void clickByPhysicalKeyBoard(int position){
        if(mainMenuAdapter!=null){
            RecyclerView.ViewHolder holder = binding.rvMenu.findViewHolderForAdapterPosition(position);
           if(holder!=null){
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
