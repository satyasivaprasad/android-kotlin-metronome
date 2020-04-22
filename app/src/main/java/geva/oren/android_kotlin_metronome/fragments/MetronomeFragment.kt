package geva.oren.android_kotlin_metronome.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import geva.oren.android_kotlin_metronome.R
import geva.oren.android_kotlin_metronome.services.MetronomeService
import geva.oren.android_kotlin_metronome.views.RotaryKnobView
import kotlinx.android.synthetic.main.metronome_fragment.*

/**
 * Main Metronome app fragment
 */
class MetronomeFragment : Fragment(),
    MetronomeService.TickListener, RotaryKnobView.RotaryKnobListener {

    private var isBound = false
    private var metronomeService: MetronomeService? = null
    private val TAG = "METRONOME_FRAGMENT"
    private var lastTapMilis: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.metronome_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindService()
        playButton.setOnClickListener() { this.play() }
        pauseButton.setOnClickListener() { this.pause() }
        rhythmButton.setOnClickListener() { this.nextRhythm() }
        toneButton.setOnClickListener() { this.nextTone() }
        tapTempoButton.setOnClickListener() { this.tapTempAction() }
        emphasisButton.setOnClickListener() {v ->
            val isEmphasis = metronomeService?.toggleEmphasis()
            beatsView.isEmphasis =  isEmphasis!!
        }
        rotaryKnob.listener = this
        setBpmText(rotaryKnob.value)
    }

    private fun tapTempAction() {
        val currentMilis = System.currentTimeMillis()
        val difference = currentMilis - lastTapMilis
        val calculatedBpm = (60000 / difference).toInt()
        val bpm = metronomeService?.setInterval(calculatedBpm)
        bpmText.text = bpm.toString()
        lastTapMilis = currentMilis
    }

    private fun bindService() {
        activity?.bindService(
            Intent(
                activity,
                MetronomeService::class.java
            ), mConnection, Context.BIND_AUTO_CREATE
        )
        isBound = true
    }

    private fun setBpmText(bpm: Int) {
        bpmText.text = if (bpm >= 100) bpm.toString() else " ${bpm.toString()}"
    }

    private fun nextTone() {
        val tone = metronomeService?.nextTone()
        if (tone != null) {
            tonesView.selectTone(tone)
        }
    }

    private fun nextRhythm() {
        val rhythm = metronomeService?.nextRhythm()
        val drawable = when (rhythm) {
            MetronomeService.Rhythm.QUARTER -> R.drawable.ic_quarter_note
            MetronomeService.Rhythm.EIGHTH -> R.drawable.ic_eighth_note
            MetronomeService.Rhythm.SIXTEENTH -> R.drawable.ic_sixteenth_note
            null -> R.drawable.ic_quarter_note
        }

        rhythmImage.setImageDrawable(
            activity?.applicationContext?.let {
                ContextCompat.getDrawable(
                    it, drawable
                )
            })
        beatsView.resetBeats(true)
    }

    private fun play() {
        beatsView.resetBeats(true)
        metronomeService?.play()
    }

    private fun pause() {
        metronomeService?.pause()
    }

    private fun updateBpm(bpm: Int) {
        metronomeService?.setInterval(bpm)
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            metronomeService = (service as MetronomeService.MetronomeBinder).getService()
            metronomeService?.addTickListener(this@MetronomeFragment)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            metronomeService = null
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            metronomeService?.removeTickListener(this)
            // Detach our existing connection.
            activity!!.unbindService(mConnection)
            isBound = false
        }
    }

    /**
     * RotaryListener interface implementation
     */
    override fun onRotate(value: Int) {
        val bpm = value
        setBpmText(bpm)
        metronomeService?.setInterval(bpm)
    }

    override fun onTick(interval: Int) {
        if (metronomeService?.isPlaying!!)
            activity?.runOnUiThread() {beatsView.nextBeat()}
    }
}
