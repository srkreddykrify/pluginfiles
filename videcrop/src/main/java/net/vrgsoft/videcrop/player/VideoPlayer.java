package net.vrgsoft.videcrop.player;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;

import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.Tracks;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoSize;


import static com.google.android.exoplayer2.C.TIME_UNSET;

public class VideoPlayer implements Player.Listener, TimeBar.OnScrubListener {
    private ExoPlayer player;


    private OnProgressUpdateListener mUpdateListener;
    private Handler progressHandler;
    private Runnable progressUpdater;


    public VideoPlayer(Context context) {




        player = new ExoPlayer.Builder(context).build();



        LoadControl loadControl = new DefaultLoadControl();
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(this);
        progressHandler = new Handler();
    }

    public void  setcenterCrop()
    {
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
    }

    public void initMediaSource(Context context, String uri) {

        player.setMediaItem(MediaItem.fromUri(uri));
        player.prepare();
        player.addListener(this);

    }
    public ExoPlayer getPlayer() {
        return player;
    }


    public void play(boolean play) {
        try {
            player.setPlayWhenReady(play);
            if (!play) {
                removeUpdater();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        try {
            player.release();
            removeUpdater();
            player = null;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public boolean isPlaying() {
        return player.getPlayWhenReady();
    }

    @Override
    public void onTimelineChanged(Timeline timeline,  int reason) {
        updateProgress();
    }



    @Override
    public void onTracksChanged(Tracks tracks) {
        Player.Listener.super.onTracksChanged(tracks);
    }



    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        Player.Listener.super.onIsLoadingChanged(isLoading);
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        Player.Listener.super.onPlaybackStateChanged(playbackState);
        try {
            updateProgress();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }



    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }


    @Override
    public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
        Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
        updateProgress();
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }




    @Override
    public void onScrubStart(TimeBar timeBar, long position) {

    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {
        seekTo(position);
        updateProgress();
    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        seekTo(position);
        updateProgress();
    }

    private void updateProgress() {
        try {
            if (mUpdateListener != null) {
                mUpdateListener.onProgressUpdate(
                        player.getCurrentPosition(),
                        player.getDuration() == TIME_UNSET ? 0L : player.getDuration(),
                        player.getBufferedPosition());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        initUpdateTimer();
    }

    private void initUpdateTimer() {
        long position = player.getCurrentPosition();
        int playbackState = player.getPlaybackState();
        long delayMs;
        if (playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
            if (player.getPlayWhenReady() && playbackState == ExoPlayer.STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }

            removeUpdater();
            progressUpdater = new Runnable() {
                @Override
                public void run() {
                    updateProgress();
                }
            };

            progressHandler.postDelayed(progressUpdater, delayMs);
        }
    }

    private void removeUpdater() {
        if (progressUpdater != null)
            progressHandler.removeCallbacks(progressUpdater);
    }

    public void seekTo(long position) {
        player.seekTo(position);
    }

    public void setUpdateListener(OnProgressUpdateListener updateListener) {
        mUpdateListener = updateListener;
    }

   /* @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        try {
            if(mUpdateListener != null){
                mUpdateListener.onFirstTimeUpdate(player.getDuration(), player.getCurrentPosition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        Player.Listener.super.onVideoSizeChanged(videoSize);

        try {
            if(mUpdateListener != null){
                mUpdateListener.onFirstTimeUpdate(player.getDuration(), player.getCurrentPosition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRenderedFirstFrame() {

    }

    public interface OnProgressUpdateListener {
        void onProgressUpdate(long currentPosition, long duration, long bufferedPosition);

        void onFirstTimeUpdate(long duration, long currentPosition);
    }
}
