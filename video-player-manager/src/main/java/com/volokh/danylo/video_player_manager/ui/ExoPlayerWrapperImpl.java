package com.volokh.danylo.video_player_manager.ui;

import android.media.MediaPlayer;

public class ExoPlayerWrapperImpl extends ExoPlayerWrapper{

    public ExoPlayerWrapperImpl() {
        super(new MediaPlayer());
    }



}
