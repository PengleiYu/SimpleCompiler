package com.example.simplecomplier

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val map = mapOf(
        "词法分析" to LexerActivity::class.java,
        "简单计算器" to CalculatorActivity::class.java
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = map.keys.toList()

        listView_main.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        listView_main.setOnItemClickListener { parent, view, position, id ->
            startActivity(Intent(this, map[list[position]]))
        }
    }
}
