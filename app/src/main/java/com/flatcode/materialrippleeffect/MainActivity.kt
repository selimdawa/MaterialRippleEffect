package com.flatcode.materialrippleeffect

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.materialrippleeffect.databinding.ActivityMainBinding
import io.selimdawa.rippleeffect.MaterialRippleEffect

class MainActivity : AppCompatActivity(), View.OnClickListener, OnLongClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rippleLayout1.setOnClickListener(this)
        binding.rippleLayout1.setOnLongClickListener(this)

        MaterialRippleEffect.on(binding.rippleLayout2).rippleColor(Color.parseColor("#FF0000"))
            .rippleAlpha(0.2f).rippleHover(true).create()

        binding.rippleLayout2.setOnLongClickListener(this)
        binding.rippleLayout2.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        Toast.makeText(this, "Short click", Toast.LENGTH_SHORT).show()
    }

    override fun onLongClick(v: View): Boolean {
        return if (v.id == binding.rippleLayout1.id) {
            Toast.makeText(this, "Long click not consumed", Toast.LENGTH_SHORT).show()
            false
        } else {
            Toast.makeText(this, "Long click and consumed", Toast.LENGTH_SHORT).show()
            true
        }
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