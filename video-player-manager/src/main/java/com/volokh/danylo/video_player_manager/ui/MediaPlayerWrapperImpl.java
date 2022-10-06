package com.volokh.danylo.video_player_manager.ui;

import android.media.MediaPlayer;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;

public class MediaPlayerWrapperImpl extends MediaPlayerWrapper{

    public MediaPlayerWrapperImpl() {
        super(new MediaPlayer());
    }



}
