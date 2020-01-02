package com.example.simplecomplier

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.simplecomplier.complier.SimpleLexer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val simpleLexer = SimpleLexer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        et_input.setText("int age = 40;")

        btn_parse.setOnClickListener {
            val s = et_input.text.toString()
            simpleLexer.tokenize(s)
            val builder = StringBuilder()
            for (token in simpleLexer.tokens) {
                builder.append(String.format("%-8s%-8s",token.text,token.type)).append("\n")
            }
            tv_display.text = builder
        }
    }
}
