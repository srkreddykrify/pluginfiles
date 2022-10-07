package com.gowtham.library.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;




import com.bumptech.glide.Glide;

import com.google.android.exoplayer2.ExoPlayer;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.gowtham.library.R;
import com.gowtham.library.utils.CompressOption;
import com.gowtham.library.utils.CustomProgressView;
import com.gowtham.library.utils.LogMessage;
import com.gowtham.library.utils.TrimVideo;
import com.gowtham.library.utils.TrimVideoOptions;
import com.gowtham.library.utils.TrimmerUtils;
import com.gowtham.library.widgets.CrystalRangeSeekbar;
import com.gowtham.library.widgets.CrystalSeekbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;



public class ActVideoTrimmer extends AppCompatActivity {

    private static final String TAG = "ActVideoTrimmer";
    public static final int RETURN_CODE_SUCCESS = 0;

    public static final int RETURN_CODE_CANCEL = 255;
    private StyledPlayerView playerView;

    private static final int PER_REQ_CODE = 115;

    private ExoPlayer videoPlayer;

    private ImageView imagePlayPause;

    private ImageView[] imageViews;

    private long totalDuration;

    //    private Dialog dialog;
//private ProgressDialog progressDialog;
    private ProgressDialog progressDialog1;
    private AppCompatDialog progressDialog;
    private Uri uri;

    private TextView txtStartDuration, txtEndDuration;

    private CrystalRangeSeekbar seekbar;

    private long lastMinValue = 0;

    private long lastMaxValue = 0;

    private MenuItem menuDone;

    private CrystalSeekbar seekbarController;

    private boolean isValidVideo = true, isVideoEnded;

    private Handler seekHandler;

    private long currentDuration, lastClickedTime;

    private CompressOption compressOption;

    private String outputPath, destinationPath;

    private int trimType;

    private long fixedGap, minGap, minFromGap, maxToGap;

    private boolean hidePlayerSeek, isAccurateCut;

    private CustomProgressView progressView;

    private String path;

    String ratio;
    Boolean iscropped;
    FFmpeg ffmpeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_video_trimmer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpToolBar(getSupportActionBar(), "Trim Video");
        toolbar.setNavigationOnClickListener(v -> finish());
        progressView = new CustomProgressView(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        playerView = findViewById(R.id.player_view_lib);
        imagePlayPause = findViewById(R.id.image_play_pause);
        seekbar = findViewById(R.id.range_seek_bar);
        txtStartDuration = findViewById(R.id.txt_start_duration);
        txtEndDuration = findViewById(R.id.txt_end_duration);
        seekbarController = findViewById(R.id.seekbar_controller);
        ImageView imageOne = findViewById(R.id.image_one);
        ImageView imageTwo = findViewById(R.id.image_two);
        ImageView imageThree = findViewById(R.id.image_three);
        ImageView imageFour = findViewById(R.id.image_four);
        ImageView imageFive = findViewById(R.id.image_five);
        ImageView imageSix = findViewById(R.id.image_six);
        ImageView imageSeven = findViewById(R.id.image_seven);
        ImageView imageEight = findViewById(R.id.image_eight);
        imageViews = new ImageView[]{imageOne, imageTwo, imageThree,
                imageFour, imageFive, imageSix, imageSeven, imageEight};
        seekHandler = new Handler();
        initPlayer();
        if (checkStoragePermission())
            setDataInView();
    }

    private void setUpToolBar(ActionBar actionBar, String title) {
        try {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPlayer() {
        try {

            videoPlayer = new ExoPlayer.Builder(this).build();

            playerView.requestFocus();
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            playerView.setPlayer(videoPlayer);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDataInView() {
        try {
            uri = Uri.parse(getIntent().getStringExtra(TrimVideo.TRIM_VIDEO_URI));
            ratio = getIntent().getStringExtra(TrimVideo.CROP_RATIO);
            iscropped = getIntent().getBooleanExtra(TrimVideo.IS_CROPPED, false);
//            uri = Uri.parse(FileUtils.getPath(this, uri));
//            uri = Uri.parse(uri);
            LogMessage.v("VideoUri:: " + uri);
            totalDuration = TrimmerUtils.getDuration(this, uri);
            imagePlayPause.setOnClickListener(v ->
                    onVideoClicked());
            playerView.getVideoSurfaceView().setOnClickListener(v ->
                    onVideoClicked());
            validate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validate() {
        try {
            TrimVideoOptions trimVideoOptions = getIntent().getParcelableExtra(TrimVideo.TRIM_VIDEO_OPTION);
            assert trimVideoOptions != null;
            trimType = TrimmerUtils.getTrimType(trimVideoOptions.trimType);
            destinationPath = trimVideoOptions.destination;
            hidePlayerSeek = trimVideoOptions.hideSeekBar;
            isAccurateCut = trimVideoOptions.accurateCut;
            compressOption = trimVideoOptions.compressOption;
            fixedGap = trimVideoOptions.fixedDuration;
            fixedGap = fixedGap != 0 ? fixedGap : totalDuration;
            minGap = trimVideoOptions.minDuration;
            minGap = minGap != 0 ? minGap : totalDuration;
            if (trimType == 3) {
                minFromGap = trimVideoOptions.minToMax[0];
                maxToGap = trimVideoOptions.minToMax[1];
                minFromGap = minFromGap != 0 ? minFromGap : totalDuration;
                maxToGap = maxToGap != 0 ? maxToGap : totalDuration;
            }
            if (destinationPath != null) {
                File outputDir = new File(destinationPath);
                outputDir.mkdirs();
                destinationPath = String.valueOf(outputDir);
                if (!outputDir.isDirectory())
                    throw new IllegalArgumentException("Destination file path error" + " " + destinationPath);
            }
            buildMediaSource(uri);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void onVideoClicked() {
        try {
            if (isVideoEnded) {
                seekTo(lastMinValue);
                videoPlayer.setPlayWhenReady(true);
                return;
            }
            if ((currentDuration - lastMaxValue) > 0)
                seekTo(lastMinValue);
            seekTo(lastMinValue);
            videoPlayer.setPlayWhenReady(!videoPlayer.getPlayWhenReady());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seekTo(long sec) {
        if (videoPlayer != null)
            videoPlayer.seekTo(sec * 1000);
    }

    private void buildMediaSource(Uri mUri) {
        try {
            videoPlayer = new ExoPlayer.Builder(this).build();
            videoPlayer.setMediaItem(MediaItem.fromUri(mUri));
            videoPlayer.prepare();

            videoPlayer.setPlayWhenReady(true);

            videoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    switch (playbackState) {
                        case Player.STATE_ENDED:
                            LogMessage.v("onPlayerStateChanged: Video ended.");
                            imagePlayPause.setVisibility(View.VISIBLE);
                            isVideoEnded = true;
                            break;
                        case Player.STATE_READY:
                            isVideoEnded = false;
                            startProgress();
                            imagePlayPause.setVisibility(videoPlayer.getPlayWhenReady() ? View.GONE :
                                    View.VISIBLE);
                            LogMessage.v("onPlayerStateChanged: Ready to play.");
                            break;
                        default:
                            break;
                    }
                }
            });

            setImageBitmaps();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setImageBitmaps() {
        try {
            long diff1 = totalDuration / 8;
            Log.e(TAG, "setImageBitmaps: " + totalDuration);
            Log.e(TAG, "setImageBitmaps: " + diff1);
            long diff = totalDuration;


            new Handler().postDelayed(() -> {
                int index = 1;
                for (ImageView img : imageViews) {
                    img.setImageBitmap(TrimmerUtils.getFrameBySec(ActVideoTrimmer.this, uri, diff * index));
                    index++;
                }
                seekbar.setVisibility(View.VISIBLE);
                txtStartDuration.setVisibility(View.VISIBLE);
                txtEndDuration.setVisibility(View.VISIBLE);
            }, 0);

            seekbarController.setMaxValue(totalDuration).apply();
            seekbar.setMaxValue(totalDuration).apply();
            seekbar.setMaxStartValue((float) totalDuration).apply();
            if (trimType == 1) {
                seekbar.setFixGap(fixedGap).apply();
                lastMaxValue = totalDuration;
            } else if (trimType == 2) {
                seekbar.setMaxStartValue((float) minGap);
                seekbar.setGap(minGap).apply();
                lastMaxValue = totalDuration;
            } else if (trimType == 3) {
                seekbar.setMaxStartValue((float) maxToGap);
                seekbar.setGap(minFromGap).apply();
                lastMaxValue = maxToGap;
            } else {
                seekbar.setGap(2).apply();
                lastMaxValue = totalDuration;
            }
            if (hidePlayerSeek)
                seekbarController.setVisibility(View.GONE);

            seekbar.setOnRangeSeekbarFinalValueListener((minValue, maxValue) -> {
                if (!hidePlayerSeek)
                    seekbarController.setVisibility(View.VISIBLE);
            });

            seekbar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
                long minVal = (long) minValue;
                long maxVal = (long) maxValue;
                if (lastMinValue != minVal) {
                    seekTo((long) minValue);
                    if (!hidePlayerSeek)
                        seekbarController.setVisibility(View.INVISIBLE);
                }
                lastMinValue = minVal;
                lastMaxValue = maxVal;
                txtStartDuration.setText(TrimmerUtils.formatSeconds(minVal));
                txtEndDuration.setText(TrimmerUtils.formatSeconds(maxVal));

                Log.e(TAG, "setImageBitmaps min , max : " + minVal + "," + maxVal);
                if (trimType == 3)
                    setDoneColor(minVal, maxVal);
            });

            seekbarController.setOnSeekbarFinalValueListener(value -> {
                long value1 = (long) value;
                if (value1 < lastMaxValue && value1 > lastMinValue) {
                    seekTo(value1);
                    return;
                }
                if (value1 > lastMaxValue)
                    seekbarController.setMinStartValue((int) lastMaxValue).apply();
                else if (value1 < lastMinValue) {
                    seekbarController.setMinStartValue((int) lastMinValue).apply();
                    if (videoPlayer.getPlayWhenReady())
                        seekTo(lastMinValue);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDoneColor(long minVal, long maxVal) {
        try {
            if (menuDone == null)
                return;
            if ((maxVal - minVal) <= maxToGap) {
                menuDone.getIcon().setColorFilter(
                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorWhite)
                                , PorterDuff.Mode.SRC_IN)
                );
                isValidVideo = true;
            } else {
                menuDone.getIcon().setColorFilter(
                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorWhiteLt)
                                , PorterDuff.Mode.SRC_IN)
                );
                isValidVideo = false;
            }

            Log.e(TAG, "setDoneColor : " + isValidVideo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PER_REQ_CODE) {
            if (isPermissionOk(grantResults))
                setDataInView();
            else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null)
            videoPlayer.release();
        if (progressView != null && progressView.isShowing())
            progressView.dismiss();
        stopRepeatingTask();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuDone = menu.findItem(R.id.action_done);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            if (SystemClock.elapsedRealtime() - lastClickedTime < 800)
                return true;
            lastClickedTime = SystemClock.elapsedRealtime();
            validateVideo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validateVideo() {

        loadFFMpegBinary(this);
        if (isValidVideo) {

            outputPath = createBaseDirectory();


            LogMessage.v("outputPath::" + outputPath);
            LogMessage.v("sourcePath::" + uri);
            LogMessage.v("sourcePath::" + uri);
            videoPlayer.setPlayWhenReady(false);
            showProcessingDialog();

//            showProgressDialog1();
            String[] complexCommand;
            if (compressOption != null)
                complexCommand = getCompressionCommand();
            else if (isAccurateCut)
                complexCommand = getAccurateBinary();
            else {



                complexCommand = new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                        "-i", String.valueOf(uri),
                        "-t",
                        TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
                        "-c:v", "libx264", "-crf", "28", "-c", "copy", outputPath};

                Log.e(TAG, "validateVideo lastMinValue : " + lastMinValue);
                Log.e(TAG, "validateVideo lastMaxValue : " + lastMaxValue);
                Log.e(TAG, "validateVideo trim lastMinValue : " + TrimmerUtils.formatCSeconds(lastMinValue));
                Log.e(TAG, "validateVideo  trim lastMaxValue - lastMinValue : " + TrimmerUtils.formatCSeconds(lastMaxValue));



//                complexCommand = new String[]{"-ss", "" + TrimmerUtils.formatCSeconds(lastMinValue), "-y", "-i", String.valueOf(uri), "-t", "" + TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), "-s", "320x240", "-r", "15", "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", outputPath};

            }
            //  execFFmpegBinary(complexCommand, true);
            execFFmpegBinary(complexCommand);
        } else {
            showVideoSizeDialog(getString(R.string.txt_smaller));
        }
    }

    private String[] getCompressionCommand() {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(String.valueOf(uri));
        String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        int w = TrimmerUtils.clearNull(width).isEmpty() ? 0 : Integer.parseInt(width);
        int h = Integer.parseInt(height);
        if (compressOption.getWidth() != 0 || compressOption.getHeight() != 0
                || !compressOption.getBitRate().equals("0k")) {
            return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri), "-s", compressOption.getWidth() + "x" +
                    compressOption.getHeight(),
                    "-r", String.valueOf(compressOption.getFrameRate()),
                    "-vcodec", "mpeg4", "-b:v",
                    compressOption.getBitRate(), "-b:a", "48000", "-ac", "2", "-ar",
                    "22050", "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), outputPath};
        } else if (w >= 800) {
            w = w / 2;
            h = Integer.parseInt(height) / 2;
            return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri),
                    "-s", w + "x" + h, "-r", "30",
                    "-vcodec", "mpeg4", "-b:v",
                    "1M", "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), outputPath};
        } else {
            return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri), "-s", w + "x" + h, "-r",
                    "30", "-vcodec", "mpeg4", "-b:v",
                    "400K", "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), outputPath};
        }
    }

    private Handler handler = new Handler();

    private void execFFmpegBinary(String[] command) {
        ffmpeg = FFmpeg.getInstance(this);

        try {

            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d("Event ", "onStart");
                }

                @Override
                public void onProgress(String message) {
                    Log.e("Event ", "onProgress - " + message);

                }

                @Override
                public void onFailure(String message) {
                    Log.e("Event ", "onFailure - " + message);
                    dismissProgressDialog();

                }

                @Override
                public void onSuccess(String message) {
                    Log.e("Event ", "onSuccess - " + message);
                    dismissProgressDialog();
//                    dialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputPath);
                    intent.putExtra(TrimVideo.CROP_RATIO, ratio);
                    intent.putExtra(TrimVideo.IS_CROPPED, iscropped);
                    setResult(RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onFinish() {
                    Log.e("Event ", "onFinish");
                    dismissProgressDialog();

                }
            });


        } catch (Exception e) {
            // Handle if FFmpeg is already running
        }
    }


   /* private void execFFmpegBinary(final String[] command, boolean retry) {
        Log.d(TAG, "execFFmpegBinary() called with: command = [" + command + "], retry = [" + retry + "]");
        try {


            String  cmd ="";
            for (int i = 0; i < command.length; i++) {

                cmd += command[i]+" ";
            }

            Log.e(TAG, "execFFmpegBinary: "+cmd );

            FFmpeg.executeAsync(command, new ExecuteCallback() {
                @Override
                public void apply(long executionId, int returnCode) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
//                        dismissProgressDialog1();
                        Log.e(Config.TAG, "Async command execution completed successfully.");
                    } else if (returnCode == RETURN_CODE_CANCEL) {
//                        dismissProgressDialog1();
                        Log.e(Config.TAG, "Async command execution cancelled by user.");
                    } else {
                        Log.e(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                    }
                }
            });



        } catch (Throwable e) {
            e.printStackTrace();
        }


    }
*/


    private String[] getAccurateBinary() {
        return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue)
                , "-i", String.valueOf(uri), "-t",
                TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
                "-async", "1", outputPath};
    }

    private void showProcessingDialog() {


        try {


            if (progressDialog != null && progressDialog.isShowing()) {
//            progressSET(message);
            } else {

                progressDialog = new AppCompatDialog(this);
                progressDialog.setCancelable(false);
                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                progressDialog.setContentView(R.layout.progress_loading);
                progressDialog.show();
                final ImageView img_loading_frame = (ImageView) progressDialog.findViewById(R.id.iv_frame_loading);

                Glide.with(this).load(R.drawable.progressloading).into(img_loading_frame);


            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void dismissProgressDialog() {

        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
//                progressDialog.dismiss();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION);
        } else
            return checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

    }

    private boolean checkPermission(String... permissions) {
        boolean allPermitted = false;
        for (String permission : permissions) {
            allPermitted = (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED);
            if (!allPermitted)
                break;
        }
        if (allPermitted)
            return true;
        ActivityCompat.requestPermissions(this, permissions,
                PER_REQ_CODE);
        return false;
    }


    private boolean isPermissionOk(int... results) {
        boolean isAllGranted = true;
        for (int result : results) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                isAllGranted = false;
                break;
            }
        }
        return isAllGranted;
    }

    void startProgress() {
        updateSeekbar.run();
    }

    void stopRepeatingTask() {
        seekHandler.removeCallbacks(updateSeekbar);
    }

    Runnable updateSeekbar = new Runnable() {
        @Override
        public void run() {
            try {
                currentDuration = videoPlayer.getCurrentPosition() / 1000;
                if (!videoPlayer.getPlayWhenReady())
                    return;
                if (currentDuration <= lastMaxValue)
                    seekbarController.setMinStartValue((int) currentDuration).apply();
                else
                    videoPlayer.setPlayWhenReady(false);
            } finally {
                seekHandler.postDelayed(updateSeekbar, 1000);
            }
        }
    };


    //filecreation

    public String createBaseDirectory() {
       /* String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File storageDir = new File(extStorageDirectory + "/" +getString(R.string.app_album_name));
        if (storageDir.mkdir()) {
            System.out.println("Directory created");
        } else {
            System.out.println("Directory is not created or exists");
        }

        return storageDir;*/

   /*  String mimeType =   getMIMEType(String.valueOf(uri));


        Log.e(TAG, "createBaseDirectory: "+mimeType );*/

        File folder = new File(String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DCIM)));
        folder.mkdirs();

        String extStorageDirectory = folder.getAbsolutePath();
        File storageDir = new File(extStorageDirectory + "/" + getString(R.string.app_trimmed_dir));
        if (storageDir.mkdir()) {
            System.out.println("Gallery Directory created");
        } else {
            System.out.println("Gallery Directory is not created or exists");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmsshhmmss");
        String date = simpleDateFormat.format(new Date());
        String name = "trimmed_" + date + ".mp4";
//        String name="trimmed_"+date+"."+mimeType;
        String file_name = storageDir.getAbsolutePath() + "/" + name;
        File new_file = new File(file_name);

        Log.e(TAG, "createBaseDirectory: " + new_file.getAbsolutePath());
        return new_file.getAbsolutePath();

       /*  String root = Environment.getExternalStorageDirectory().toString();
        String app_folder = root + "/GFG/";



        String filePrefix = "trimmed_video_";
        String fileExtn = ".mp4";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentValues valuesvideos = new ContentValues();
            valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "Folder");
            valuesvideos.put(MediaStore.Video.Media.TITLE, filePrefix + System.currentTimeMillis());
            valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, filePrefix + System.currentTimeMillis() + fileExtn);
            valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, valuesvideos);
            filePath = FileUtils.getPath(this, uri);
//            filePath = file.getAbsolutePath();

            return filePath;

        } else {
            File dest = new File(new File(app_folder), filePrefix + fileExtn);
            int fileNo = 0;
            while (dest.exists()) {
                fileNo++;
                dest = new File(new File(app_folder), filePrefix + fileNo + fileExtn);
            }
            filePath = dest.getAbsolutePath();

            return filePath;
        }*/


    }

    private void showProgressDialog1() {

//        if (isProgressVisible) {

//            progressDialog = new ProgressDialog(context);
        progressDialog1 = new ProgressDialog(this, R.style.StyledDialog);
        ;
        progressDialog1.show();
//            progressDialog.setIndeterminateDrawable(context.getDrawable(R.drawable.grey_background));
        progressDialog1.setCanceledOnTouchOutside(false);

//        }
    }

    private void dismissProgressDialog1() {

        try {
//            if (isProgressVisible) {
            progressDialog1.dismiss();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showVideoSizeDialog(String meassge) {


        final Dialog alertDialog = new Dialog(this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setContentView(R.layout.dialog_alert_ok);

        TextView message_tv = (TextView) alertDialog.findViewById(R.id.message_tv);
        TextView ok_tv = (TextView) alertDialog.findViewById(R.id.ok_tv);


        message_tv.setText(meassge);
        ok_tv.setText("OK ");
        ok_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.cancel();


            }
        });


        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(true);

    }


    // url = file path or suitable URL.
    public static String getMIMEType(String url) {
        String mType = null;
        String mExtension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (mExtension != null) {
            mType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mExtension);
        }
        return mType.replace("video/", "");
    }


    public static void loadFFMpegBinary(final Activity activity) {

        Log.e(TAG, "loadFFMpegBinary: " + activity.getLocalClassName());
        try {


            if (nl.bravobit.ffmpeg.FFmpeg.getInstance(activity).isSupported()) {
                Log.e(TAG, "loadFFMpegBinary: true");
                // ffmpeg is supported
            } else {

                Log.e(TAG, "loadFFMpegBinary: false");
                // ffmpeg is not supported
            }
        } catch (Exception exception) {

        }


    }
}