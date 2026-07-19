package com.flatcode.materialrippleeffect

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.flatcode.materialrippleeffect.databinding.ActivityMainBinding
import io.selimdawa.rippleeffect.materialRipple

class MainActivity : AppCompatActivity(), View.OnClickListener, OnLongClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // 1. Color & Alpha Demo
        binding.demoColorAlpha.materialRipple {
            rippleColor(ContextCompat.getColor(this@MainActivity, R.color.ripple_red))
            rippleAlpha(0.5f)
        }

        // 2. Custom Duration Demo
        binding.demoDuration.materialRipple {
            rippleDuration(150)
            rippleFadeDuration(50)
        }

        // 3. Large Diameter Demo
        binding.demoDiameter.materialRipple {
            rippleDiameterDp(200)
        }

        // 4. Overlay Disabled Demo
        binding.demoNoOverlay.materialRipple {
            rippleOverlay(false)
            rippleColor(ContextCompat.getColor(this@MainActivity, R.color.ripple_blue))
        }

        // 5. Hover Disabled Demo
        binding.demoNoHover.materialRipple {
            rippleHover(false)
        }

        // 6. Delayed Click Demo
        binding.demoDelayedClick.materialRipple {
            rippleDelayClick(true)
            rippleDuration(2000)
        }

        // 7. Persistent Ripple Demo
        binding.demoPersistent.materialRipple {
            ripplePersistent(true)
            rippleColor(ContextCompat.getColor(this@MainActivity, R.color.ripple_magenta))
        }

        // 8. Rounded Corners Demo
        binding.demoRounded.materialRipple {
            rippleRoundedCorners(32)
            rippleColor(ContextCompat.getColor(this@MainActivity, R.color.ripple_green))
        }

        // 9. Manual Trigger (Center) Demo
        val manualRipple = binding.demoManual.materialRipple {
            rippleHover(false)
        }
        binding.demoManual.setOnClickListener {
            manualRipple.performRipple()
            showToast(getString(R.string.toast_manual_trigger))
        }

        // 10. Manual Trigger (Point) Demo
        val manualPointRipple = binding.demoManualPoint.materialRipple {
            rippleHover(false)
        }
        binding.demoManualPoint.setOnClickListener {
            manualPointRipple.performRipple(Point(0, 0))
            showToast(getString(R.string.toast_manual_point))
        }

        // 11. Dynamic Background Demo
        val dynamicRipple = binding.demoDynamicBg.materialRipple {
            rippleBackground(ContextCompat.getColor(this@MainActivity, R.color.ripple_gray_demo))
        }
        binding.demoDynamicBg.setOnClickListener {
            dynamicRipple.setRippleBackground(ContextCompat.getColor(this, R.color.ripple_light_green))
            showToast(getString(R.string.toast_bg_changed))
        }

        val commonButtons = listOf(
            binding.demoColorAlpha, binding.demoDuration, binding.demoDiameter,
            binding.demoNoOverlay, binding.demoNoHover, binding.demoDelayedClick,
            binding.demoPersistent, binding.demoRounded
        )

        commonButtons.forEach {
            it.setOnClickListener(this)
            it.setOnLongClickListener(this)
        }
        
        binding.demoManual.setOnLongClickListener(this)
        binding.demoManualPoint.setOnLongClickListener(this)
        binding.demoDynamicBg.setOnLongClickListener(this)
    }

    override fun onClick(v: View?) {
        showToast(getString(R.string.toast_short_click))
    }

    override fun onLongClick(v: View): Boolean {
        val btnText = (v as? com.google.android.material.button.MaterialButton)?.text?.toString() ?: ""
        showToast(getString(R.string.toast_long_click_on, btnText))
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_button, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.switch_list -> {
                startActivity(Intent(this, ListActivity::class.java))
                finish()
                true
            }
            R.id.switch_recycler -> {
                startActivity(Intent(this, RecyclerActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}