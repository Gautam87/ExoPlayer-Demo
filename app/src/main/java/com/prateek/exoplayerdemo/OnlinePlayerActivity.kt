package com.prateek.exoplayerdemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.prateek.exoplayerdemo.databinding.ActivityMainBinding
import com.prateek.exoplayerdemo.manager.DemoUtil
import com.prateek.exoplayerdemo.manager.DownloadTracker

class OnlinePlayerActivity : AppCompatActivity(), Player.Listener {
    private var player: ExoPlayer? = null
    private var playbackPosition = 0L
    private var playWhenReady = true
    private lateinit var binding: ActivityMainBinding
    private var downloadTracker: DownloadTracker? = null

    //    private val mediaItem by lazy {
//        MediaItem.Builder()
//            .setUri(VIDEO_URL)
//            .setMediaId("dummyId")
//            .setMediaMetadata(
//                MediaMetadata.Builder()
//                    .setTitle("Demo Video")
//                    .build()
//            )
//            .build()
//    }
    private val mediaItem by lazy {
        val drmConfig =
            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(LICENSE_URL)
        MediaItem.Builder()
            .setUri(VIDEO_URL)
            .setMediaId("dummyId")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Demo Video")
                    .build()
            )
            .setDrmConfiguration(drmConfig.build()).build()
    }

    companion object {
        const val VIDEO_URL =
            "https://prod-pocketfm-cors-header.s3.ap-southeast-1.amazonaws.com/test_widevine/h264.mpd"
        const val LICENSE_URL =
            "https://widevine.gumlet.com/licence/63a5589d669e99d3ded2b8e9?expires=1694668798601&rental_duration=300&token=e227e0ea97d4af40a9192a127c35e35dcea26bba"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        downloadTracker = DemoUtil.getDownloadTracker(this)
        binding.download.setOnClickListener {
            val renderersFactory = DemoUtil.buildRenderersFactory(this)
            downloadTracker?.toggleDownload(
                supportFragmentManager,
                mediaItem,
                renderersFactory
            )
        }
        binding.playOffline.setOnClickListener {
            if (downloadTracker?.isDownloaded(mediaItem) == true) {
                startActivity(OfflinePlayerActivity.getIntent(this))
            } else {
                Toast.makeText(this, "Video Not Downloaded yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build()
        player?.playWhenReady = true
        binding.playerExo.player = player
////        val defaultHttpDataSourceFactory = DemoUtil.getDataSourceFactory(this)
////        val mediaSource =
////            HlsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(mediaItem)
//        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
//        val drmConfig =
//            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
//                .setLicenseUri("https://widevine.gumlet.com/licence/63a5589d669e99d3ded2b8e9?expires=1694614883183&token=1df4cf6c716313696e6cf2563ee9b220f447985a")
//        val mediaItem = MediaItem.Builder()
//            .setUri("https://prod-pocketfm-cors-header.s3.ap-southeast-1.amazonaws.com/test_widevine/h264.mpd")
//            .setDrmConfiguration(drmConfig.build())
//        val mediaSource =
//            DashMediaSource.Factory(defaultHttpDataSourceFactory)
//                .createMediaSource(mediaItem.build())
//        player?.setMediaSource(mediaSource)
//        player?.seekTo(playbackPosition)
//        player?.playWhenReady = playWhenReady
//        player?.prepare()

    }

    private fun releasePlayer() {
        player?.let {
            playbackPosition = it.currentPosition
            playWhenReady = it.playWhenReady
            it.release()
            player = null
        }
    }


    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initPlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}