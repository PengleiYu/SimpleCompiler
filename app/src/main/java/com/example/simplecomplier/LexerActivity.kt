package com.example.simplecomplier

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.simplecomplier.complier.SimpleLexer
import kotlinx.android.synthetic.main.layout_repl.*

class LexerActivity : AppCompatActivity() {
    private val simpleLexer = SimpleLexer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_repl)

        et_input_repl.setText("int age = 40;")

        btn_parse_repl.setOnClickListener {
            val s = et_input_repl.text.toString()
            val tokenize = simpleLexer.tokenize(s)
            val builder = StringBuilder()
            while (tokenize.peek() != null) {
                val token = tokenize.read()
                builder.append(String.format("%-8s%-8s", token.text, token.type)).append("\n")
            }
            tv_display_repl.text = builder
        }
    }
}
