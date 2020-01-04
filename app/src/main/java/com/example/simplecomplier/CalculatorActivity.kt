package com.example.simplecomplier

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.simplecomplier.complier.SimpleCalculator
import kotlinx.android.synthetic.main.layout_repl.*
import java.lang.Exception

class CalculatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_repl)

        et_input_repl.setText("1 + 8 * 2 ")
        val calculator = SimpleCalculator()
        btn_parse_repl.setOnClickListener {
            val script: String = et_input_repl.text.toString()
            try {
                val result = calculator.evaluate(script)
                tv_display_repl.text = result.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}