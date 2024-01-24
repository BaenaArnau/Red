package com.example.red;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.MediaController;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

public class MediaFragment extends Fragment {
    VideoView videoView;
    ImageView imageView;
    public AppViewModel appViewModel;
    public MediaFragment() {
// Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle
            savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appViewModel = new
                ViewModelProvider(requireActivity()).get(AppViewModel.class);
        imageView = view.findViewById(R.id.imageView);
        videoView = view.findViewById(R.id.videoView);
        appViewModel.postSeleccionado.observe(getViewLifecycleOwner(), post ->
        {
            if ("video".equals(post.mediaType) ||
                    "audio".equals(post.mediaType)) {
                MediaController mc = new MediaController(requireContext());
                mc.setAnchorView(videoView);
                videoView.setMediaController(mc);
                videoView.setVideoPath(post.mediaUrl);
                videoView.start();
            } else if ("image".equals(post.mediaType)) {
                Glide.with(requireView()).load(post.mediaUrl).into(imageView);
            }
        });
    }
}