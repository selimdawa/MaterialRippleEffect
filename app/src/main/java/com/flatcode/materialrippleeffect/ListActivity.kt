package com.flatcode.materialrippleeffect

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.materialrippleeffect.databinding.ListBinding
import java.util.UUID

class ListActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var binding: ListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.list.adapter = ArrayAdapter(
            this, R.layout.list_item, android.R.id.text1, data
        )

        binding.list.onItemClickListener = this
        binding.list.onItemLongClickListener = OnItemLongClickListener { _, _, position, _ ->
            if (position % 2 == 0) {
                Toast.makeText(
                    this@ListActivity,
                    getString(R.string.toast_long_item_not_consumed, position),
                    Toast.LENGTH_SHORT
                ).show()
                false
            } else {
                Toast.makeText(
                    this@ListActivity,
                    getString(R.string.toast_long_item_consumed, position),
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Toast.makeText(
            this, getString(R.string.toast_rippled_item, position), Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.switch_button -> {
                startActivity(Intent(this, MainActivity::class.java))
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

    companion object {
        private val data: Array<String?> = Array(50) { UUID.randomUUID().toString() }
    }
}