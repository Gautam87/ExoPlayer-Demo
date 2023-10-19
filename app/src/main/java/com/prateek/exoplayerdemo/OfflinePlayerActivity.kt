package com.prateek.exoplayerdemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.DrmSessionEventListener
import androidx.media3.exoplayer.drm.OfflineLicenseHelper
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.offline.DownloadHelper
import com.prateek.exoplayerdemo.databinding.ActivityMainBinding
import com.prateek.exoplayerdemo.manager.DemoUtil
import com.prateek.exoplayerdemo.manager.DownloadTracker
import com.prateek.exoplayerdemo.manager.VideoDrmKeyManager
import com.prateek.exoplayerdemo.manager.VideoDrmKeyManager.Companion.KEY_SETTINGS
import com.prateek.exoplayerdemo.manager.VideoDrmKeyManager.Companion.KEY_WIDEVINE

class OfflinePlayerActivity : AppCompatActivity(), Player.Listener {
    private var player: ExoPlayer? = null
    private var playbackPosition = 0L
    private var playWhenReady = true
    private lateinit var binding: ActivityMainBinding
    private var downloadTracker: DownloadTracker? = null
    private val drmKeyManager by lazy {
        VideoDrmKeyManager(this, KEY_SETTINGS)
    }
    private val eventDispatcher by lazy {
        DrmSessionEventListener.EventDispatcher()
    }

    companion object {
        const val VIDEO_URL =
            "https://prod-pocketfm-cors-header.s3.ap-southeast-1.amazonaws.com/test_vp9_codec/output.m3u8"

        fun getIntent(context: Context): Intent {
            return Intent(context, OfflinePlayerActivity::class.java)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        downloadTracker = DemoUtil.getDownloadTracker(this)
        binding.download.visibility = View.GONE
        binding.playOffline.visibility = View.GONE
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build()
        player?.playWhenReady = true
        binding.playerExo.player = player

//        val mediaSource = downloadTracker?.getDownloadRequest(Uri.parse(VIDEO_URL))!!.let {
//            DownloadHelper.createMediaSource(
//                it,
//                DemoUtil.getDataSourceFactory(this)
//            )
//        }

//        setDrmConfiguration(
//            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
//                .setKeySetId(
//                    VideoDrmKeyManager(context, VideoDrmKeyManager.KEY_SETTINGS).saveKeySetId(
//                        VideoDrmKeyManager.KEY_WIDEVINE, keySetId
//                    ))
//                .build()
//        )

//        if(!hasValidWidevineLicense()){
//            Toast.makeText(this, "License Expired", Toast.LENGTH_SHORT).show()
//        }
//        val drmConfig =
//            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
//                .setKeySetId(drmKeyManager.getKeySetId(KEY_WIDEVINE))
        val mediaItem = MediaItem.Builder()
            .setUri(VIDEO_URL)
//            .setDrmConfiguration(drmConfig.build())
        val mediaSource =
            HlsMediaSource.Factory(DemoUtil.getDataSourceFactory(this))
                .createMediaSource(mediaItem.build())
        player?.setMediaSource(mediaSource)
        player?.seekTo(playbackPosition)
        player?.playWhenReady = playWhenReady
        player?.prepare()

    }

    fun hasValidWidevineLicense(): Boolean = try {
        drmKeyManager.getKeySetId(KEY_WIDEVINE)?.let { bytes ->
            OfflineLicenseHelper.newWidevineInstance(
                "",
                DemoUtil.getDataSourceFactory(this),
                eventDispatcher
            ).getLicenseDurationRemainingSec(bytes).also {
                Toast.makeText(this, "Remaining "+it.first+" sec", Toast.LENGTH_SHORT).show()
                return it.first > 0
            }
        }
        false
    } catch (e: Exception) {
        e.printStackTrace()
        false
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