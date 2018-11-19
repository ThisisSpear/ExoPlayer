package com.example.geonchang.mytestapp;

import android.app.Dialog;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements VideoRendererEventListener {


    private static final String TAG = "MainActivityLog";
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;

    private TextView resolutionTextView;
    private TextView txt_ct, txt_td;

    private Runnable updatePlayer;

    private Handler mainHandler;

    private ConcatenatingMediaSource concatenatedSource;

    private Dialog mFullScreenDialog;
    private boolean mExoPlayerFullscreen = false;
    private ImageView mFullScreenIcon;
    private FrameLayout mFullScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }//End of onCreate

    {
        updatePlayer = new Runnable() {
            @Override
            public void run() {
                String totDur = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(player.getDuration()),
                        TimeUnit.MILLISECONDS.toMinutes(player.getDuration()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getDuration())), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
                String curDur = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition()),
                        TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition())), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition())));
                txt_ct.setText(curDur);
                txt_td.setText(totDur);

                mainHandler.postDelayed(updatePlayer, 200);
            }
        };
    }


    @Override
    public void onVideoEnabled(DecoderCounters counters) {
        Log.v(TAG, "onRenderedFirstFrame [" + " counters: " + counters + "]");
    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        Log.v(TAG, "onVideoDecoderInitialized [" + " decoderName: " + decoderName + " initializedTimestampMs: " + initializedTimestampMs + " initializationDurationMs: " + initializationDurationMs + "]");
    }

    @Override
    public void onVideoInputFormatChanged(Format format) {
        Log.v(TAG, "onVideoInputFormatChanged [" + " format: " + format + "]");
    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {
        Log.v(TAG, "onDroppedFrames [" + " count: " + count + "]");
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        Log.v(TAG, "onVideoSizeChanged [" + " width: " + width + " height: " + height + "]");
        resolutionTextView.setText("RES:(WxH) : " + width + "X" + height + " (" + height + "p)");
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {
        Log.v(TAG, "onRenderedFirstFrame [" + " surface: " + surface + "]");
    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {
        Log.v(TAG, "onVideoDisabled [" + " counters: " + counters + "]");
        resolutionTextView.setText("RADIO 생방송 진행중");
    }

    private void initFullScreenDialog() {
        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }

    private void openFullscreenDialog() {
        ((ViewGroup) simpleExoPlayerView.getParent()).removeView(simpleExoPlayerView);
        mFullScreenDialog.addContentView(simpleExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }

    private void closeFullscreenDialog() {
        ((ViewGroup) simpleExoPlayerView.getParent()).removeView(simpleExoPlayerView);
        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(simpleExoPlayerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fullscreen_expand));
    }

    private void initFullscreenButton() {
        PlaybackControlView controlView = simpleExoPlayerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });
    }

//-------------------------------------------------------ANDROID LIFECYCLE---------------------------------------------------------------------------------------------

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()...");
        if (resolutionTextView == null) {

            resolutionTextView = (TextView) findViewById(R.id.resolution_textView);

            mainHandler = new Handler();
            simpleExoPlayerView = new SimpleExoPlayerView(this);
            simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
            txt_ct = (TextView) findViewById(R.id.txt_currentTime);
            txt_td = (TextView) findViewById(R.id.txt_totalDuration);

            initFullScreenDialog();
            initFullscreenButton();

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 링크 사용
            Uri mp4VideoUri2 = Uri.parse("http://onair1.cpbc.co.kr:1935/live/Instreamer.stream/playlist.m3u8");

            // SD 카드에서 비디오 전송
            // (파일 및 경로를 설정한 다음 파일을 가져오려면 비디오 소스를 변경하십시오.)
            String urimp4 = "/DCIM/Wildlife.mp4"; //upload file to device and add path/name.mp4
            Uri mp4VideoUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + urimp4);
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // 재생 중 대역폭 측정, 필수가 아닌 경우 null도 가능.
            DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
            // 미디어 데이터가 로드되는 데이터소스 객체 생성
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);
            // 미디어 데이터를 구문 분석하기위한 ExTractors 객체 생성
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 재생할 미디어를 나타내는 미디어 소스.

            // SD카드 사용 (MP4파일재생)
            MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);

            // 링크 사용 (Hls파일재생)
            //MediaSource videoSource = new HlsMediaSource(mp4VideoUri, dataSourceFactory, 1, null, null);
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


            // 재생목록 만들기
            MediaSource firstSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
            MediaSource secondSource = new HlsMediaSource(mp4VideoUri2, dataSourceFactory, 1, null, null);
            // 플레이 할 미디어 소스를 순서대로 넣어준다.
            concatenatedSource = new ConcatenatingMediaSource(firstSource, secondSource);

            //final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);

        }
        // 트랙셀렉터 만들기
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 로드컨트롤러 만들기
        LoadControl loadControl = new DefaultLoadControl();

        // 플레이어 만들기
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);


        // 미디어 컨트롤러 설정
        simpleExoPlayerView.setUseController(true);
        simpleExoPlayerView.requestFocus();

        // 뷰에 플레이어 연결
        simpleExoPlayerView.setPlayer(player);
        player.setPlayWhenReady(true); // 재생 준비가 되면 file/link 실행
        player.setVideoDebugListener(this); // 해상도 변경 청취 및 해상도 출력
        mainHandler.postDelayed(updatePlayer, 200);

        // 소스를 사용하여 플레이어 준비
        player.prepare(concatenatedSource);

        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                Log.v(TAG, "Listener-onTimelineChanged...");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "Listener-onTracksChanged...");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.v(TAG, "Listener-onLoadingChanged...isLoading:" + isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState);
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.v(TAG, "Listener-onRepeatModeChanged...");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "Listener-onPlayerError...");
                player.stop();
                //player.prepare(loopingSource);
                player.setPlayWhenReady(true);
            }

            @Override
            public void onPositionDiscontinuity() {
                Log.v(TAG, "Listener-onPositionDiscontinuity...");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.v(TAG, "Listener-onPlaybackParametersChanged...");
            }
        });

        if (mExoPlayerFullscreen) {
            ((ViewGroup) simpleExoPlayerView.getParent()).removeView(simpleExoPlayerView);
            mFullScreenDialog.addContentView(simpleExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fullscreen_skrink));
            mFullScreenDialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()...");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()...");
        player.release();
    }
}

