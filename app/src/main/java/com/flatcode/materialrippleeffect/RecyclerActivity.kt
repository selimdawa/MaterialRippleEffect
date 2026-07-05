package com.flatcode.materialrippleeffect

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.materialrippleeffect.databinding.RecyclerBinding
import io.selimdawa.rippleeffect.MaterialRippleEffect
import java.util.UUID

class RecyclerActivity : AppCompatActivity() {

    private lateinit var binding: RecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = MyAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_recycler, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.switch_button -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }

            R.id.switch_list -> {
                startActivity(Intent(this, ListActivity::class.java))
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val inflater = LayoutInflater.from(viewGroup.context)
            val view = inflater.inflate(R.layout.recycler_item, viewGroup, false)

            val rippleView = MaterialRippleEffect.on(view).rippleOverlay(true).rippleAlpha(0.2f)
                .rippleColor(-0xa7a7a8).rippleHover(true).create()

            return MyViewHolder(rippleView)
        }

        override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
            viewHolder.text.text = data[position]
        }

        override fun getItemCount(): Int = data.size
    }

    private class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, OnLongClickListener {

        val text: TextView = itemView.findViewById(R.id.text1)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            Toast.makeText(
                v.context, "Rippled item: $bindingAdapterPosition", Toast.LENGTH_SHORT
            ).show()
        }

        override fun onLongClick(v: View): Boolean {
            val position = bindingAdapterPosition
            return if (position % 2 == 0) {
                Toast.makeText(
                    v.context, "long item: $position and not consumed", Toast.LENGTH_SHORT
                ).show()
                false
            } else {
                Toast.makeText(
                    v.context, "long item: $position and consumed", Toast.LENGTH_SHORT
                ).show()
                true
            }
        }
    }

    companion object {
        private val data: Array<String> = Array(50) { UUID.randomUUID().toString() }
    }
}