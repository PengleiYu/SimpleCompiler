package com.example.simplecomplier

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.simplecomplier.complier.SimpleParser
import com.example.simplecomplier.complier.SimpleScript
import kotlinx.android.synthetic.main.layout_repl.*
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

class CalculatorActivity : AppCompatActivity() {
    private val parser = SimpleParser()
    private var scriptTxt = StringBuilder()
    private val simpleScript = SimpleScript()

    private val hintSymbol = "> "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_repl)
        et_input_repl.setText("int a = 100;")
        et_input_repl.hint = "输入分号执行语句"
        tv_display_repl.text = hintSymbol
        checkbox_repl.setOnCheckedChangeListener { _, isChecked ->
            SimpleScript.verbose = isChecked
        }

        val printer = Printer()
        System.setOut(printer)
        btn_parse_repl.setOnClickListener {
            val line: String = et_input_repl.text.toString()
            readLine(line)
            et_input_repl.text = null
        }
    }

    private fun readLine(line: String) {
        if ("exit()" == line) {
            return
        }
        scriptTxt.append(line).append("\n")
        System.out.println(line)
        if (line.endsWith(";")) {//分号结尾才开始执行
            try {
                val node = parser.parse(scriptTxt.toString())
//                if (SimpleScript.verbose) {
//                    parser.dumpAST(node, "")
//                }
                val result: Int? = simpleScript.evaluate(node, "")
                System.out.println(result.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                System.out.println("Error: " + e.message)
            }
            scriptTxt = StringBuilder()
            System.out.print(hintSymbol)
        }
    }

    inner class Printer : PrintStream {

        constructor() : super(File.createTempFile("script", ""))
        constructor(out: OutputStream) : super(out)
        constructor(out: OutputStream, autoFlush: Boolean) : super(out, autoFlush)
        constructor(fileName: String) : super(fileName)
        constructor(fileName: String, csn: String) : super(fileName, csn)
        constructor(file: File) : super(file)
        constructor(file: File, csn: String) : super(file, csn)
        constructor(out: OutputStream?, autoFlush: Boolean, encoding: String?) : super(out, autoFlush, encoding)

        override fun println() {
            tv_display_repl.append("\n")
            scroll()
        }

        override fun println(x: String?) {
            tv_display_repl.append(x)
            tv_display_repl.append("\n")
            scroll()
        }

        override fun print(s: String?) {
            tv_display_repl.append(s)
            scroll()
        }
    }

    private fun scroll() {
        scrollView_repl.post {
            scrollView_repl.scrollTo(0, tv_display_repl.bottom)
        }
    }
}