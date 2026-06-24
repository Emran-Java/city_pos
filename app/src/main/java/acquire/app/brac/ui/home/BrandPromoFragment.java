package acquire.app.brac.ui.home;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.zztl.pos.city.R;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;

public class BrandPromoFragment extends Fragment {

    private static final String ARG_TITLE = "title";

    private ImageView ivHomSlidPromo;
    private ImageView ivHomGifSlidPromo;
    private VideoView ivHomVideoSlidPromo;

    private ImageView btnMute;
    private MediaPlayer mediaPlayer;
    private boolean isMuted = true;


    public static BrandPromoFragment newInstance(String title) {
        BrandPromoFragment fragment = new BrandPromoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_promo_page, container, false);

        ivHomSlidPromo = view.findViewById(R.id.ivHomSlidPromo);
        ivHomSlidPromo.setVisibility(View.GONE);
        ivHomGifSlidPromo = view.findViewById(R.id.ivHomGifSlidPromo);
        ivHomGifSlidPromo.setVisibility(View.GONE);
        ivHomVideoSlidPromo = view.findViewById(R.id.ivHomVideoSlidPromo);
        ivHomVideoSlidPromo.setVisibility(View.GONE);

        CustomMediaController controller =
                new CustomMediaController(requireContext());

        //controller.setAnchorView(ivHomVideoSlidPromo);
//        ivHomVideoSlidPromo.setMediaController(controller);
        ivHomVideoSlidPromo.setMediaController(null);
        ivHomVideoSlidPromo.setOnTouchListener((v, event) -> true);

        btnMute = view.findViewById(R.id.btnMute);
        btnMute.setVisibility(View.GONE);

        displayMedia(getArguments().getString(ARG_TITLE));

        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMuted) {
                    if(mediaPlayer!=null)
                        mediaPlayer.setVolume(0.5f, 0.5f);
                    btnMute.setAlpha(0.5f);
                }
                else{
                    mediaPlayer.setVolume(0f, 0f);
                    btnMute.setAlpha(1f);
                }
                isMuted = !isMuted;
            }
        });

        //TextView tv = view.findViewById(R.id.tvTitle);
        //tv.setText(getArguments().getString(ARG_TITLE));

        return view;
    }

    private void displayMedia(String path) {
        File file = new File(path);

        if (!file.exists()) return;

        String extension = getFileExtension(path);

        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "png":
                btnMute.setVisibility(View.GONE);
                ivHomSlidPromo.setVisibility(View.VISIBLE);
                Glide.with(this).load(file).into(ivHomSlidPromo);
                break;

            case "gif":
                btnMute.setVisibility(View.GONE);
                ivHomGifSlidPromo.setVisibility(View.VISIBLE);
                // Glide automatically detects and plays GIFs
                Glide.with(this).asGif().load(file).into(ivHomGifSlidPromo);
                break;

            case "mp4":{
                btnMute.setVisibility(View.VISIBLE);
                ivHomVideoSlidPromo.setVisibility(View.VISIBLE);
                setupVideo(path);
                break;
            }
        }
    }

    private void setupVideo(String path) {
        ivHomVideoSlidPromo.setVideoPath(path);

        // Add Play/Pause controls
        MediaController mediaController = new MediaController(getContext());
        mediaController.setAnchorView(ivHomVideoSlidPromo);
        ivHomVideoSlidPromo.setMediaController(mediaController);

        ivHomVideoSlidPromo.setOnPreparedListener(mp -> {
            mediaPlayer = mp;
            mediaPlayer.setVolume(0f, 0f);
            isMuted = true;
            btnMute.setAlpha(1.0f);

            mediaPlayer.setLooping(true);

            ivHomVideoSlidPromo.start();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ivHomVideoSlidPromo != null && ivHomVideoSlidPromo.isPlaying()) {
            ivHomVideoSlidPromo.pause();

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ivHomVideoSlidPromo != null) {
            ivHomVideoSlidPromo.start();
            ivHomVideoSlidPromo.setMediaController(null);
        }
    }

    private String getFileExtension(String path) {
        int lastIndexOf = path.lastIndexOf(".");
        if (lastIndexOf == -1) return "";
        return path.substring(lastIndexOf + 1);
    }
}