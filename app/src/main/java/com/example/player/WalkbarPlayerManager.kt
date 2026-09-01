package com.example.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WalkbarPlayerManager(
  private val context: Context,
  private val coroutineScope: CoroutineScope
) {

  private var player: ExoPlayer? = null
  private var positionUpdateJob: Job? = null

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _currentPositionMs = MutableStateFlow(0L)
  val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

  private val _durationMs = MutableStateFlow(0L)
  val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

  private val playerListener = object : Player.Listener {
    override fun onIsPlayingChanged(isPlaying: Boolean) {
      _isPlaying.value = isPlaying
      if (isPlaying) {
        startPositionTracker()
      } else {
        stopPositionTracker()
        player?.let { _currentPositionMs.value = it.currentPosition }
      }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
      when (playbackState) {
        Player.STATE_READY -> {
          player?.let {
            _durationMs.value = it.duration.coerceAtLeast(0L)
            _currentPositionMs.value = it.currentPosition
          }
        }
        Player.STATE_ENDED -> {
          _isPlaying.value = false
        }
        else -> Unit
      }
    }
  }

  fun initialize(uri: Uri, initialDuration: Long = 0L) {
    release()

    _durationMs.value = initialDuration
    _currentPositionMs.value = 0L

    val exoPlayer = ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(uri))
      repeatMode = Player.REPEAT_MODE_ALL
      addListener(playerListener)
      prepare()
      playWhenReady = true
    }

    player = exoPlayer
  }

  fun getPlayer(): ExoPlayer? = player

  fun play() {
    player?.play()
  }

  fun pause() {
    player?.pause()
    player?.let { _currentPositionMs.value = it.currentPosition }
  }

  fun togglePlayPause() {
    if (_isPlaying.value) {
      pause()
    } else {
      play()
    }
  }

  fun seekTo(positionMs: Long) {
    val duration = _durationMs.value.coerceAtLeast(1L)
    val clamped = positionMs.coerceIn(0L, duration)
    _currentPositionMs.value = clamped
    player?.seekTo(clamped)
  }

  private fun startPositionTracker() {
    positionUpdateJob?.cancel()
    positionUpdateJob = coroutineScope.launch(Dispatchers.Main) {
      while (isActive && _isPlaying.value) {
        player?.let {
          _currentPositionMs.value = it.currentPosition
          if (it.duration > 0 && it.duration != _durationMs.value) {
            _durationMs.value = it.duration
          }
        }
        delay(16) // ~60fps sync rate
      }
    }
  }

  private fun stopPositionTracker() {
    positionUpdateJob?.cancel()
    positionUpdateJob = null
  }

  fun release() {
    stopPositionTracker()
    player?.removeListener(playerListener)
    player?.release()
    player = null
    _isPlaying.value = false
  }
}
