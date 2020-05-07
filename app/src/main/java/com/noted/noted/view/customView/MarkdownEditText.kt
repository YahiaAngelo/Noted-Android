package com.noted.noted.view.customView

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.MetricAffectingSpan
import android.text.style.QuoteSpan
import android.text.style.StrikethroughSpan
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.core.text.getSpans
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap
import com.noted.noted.R
import io.noties.markwon.Markwon
import io.noties.markwon.core.spans.*
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin

class MarkdownEditText : TextInputEditText {

    var markwon: Markwon
    private var textWatcher: TextWatcher? = null

    constructor(context: Context) : super(context, null) {
        markwon = Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .build()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs,
        R.attr.editTextStyle
    ) {
        markwon = Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .build()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        wrap(
            context,
            attrs,
            defStyleAttr,
            0
        ), attrs, defStyleAttr
    ) {
        markwon = Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .build()
    }



    fun triggerStyle(textStyle: TextStyle, stop: Boolean) {
        if (stop) {
            removeTextChangedListener(textWatcher)
        } else {
            textWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {


                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    styliseText(textStyle, s, start, before, count)

                }

            }
            addTextChangedListener(textWatcher)
        }


    }
    fun addQuote(){
        text!!.append("\n  ")
        val quoteSpan = QuoteSpan(context.resources.getColor(R.color.divider, context.theme), 10, 28)
        text!!.setSpan(quoteSpan, text!!.length - 2, text!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    }

    fun triggerUnOrderedListStyle(stop : Boolean){
        if (stop){
            setOnEditorActionListener(null)
        }else{
            if (text!!.isNotEmpty()){
                if (text.toString().substring(text!!.length - 2, text!!.length) != "\n"){
                    text!!.append("\n  ")
                }else{
                    text!!.append("  ")
                }
            }else{
                text!!.append("  ")
            }


            text!!.setSpan(BulletListItemSpan(markwon.configuration().theme(), 0), text!!.length - 2, text!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            setOnEditorActionListener { v, actionId, event ->

                if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN ))){
                    text!!.append("\n  ")
                    text!!.setSpan(BulletListItemSpan(markwon.configuration().theme(), 0), text!!.length - 2, text!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    return@setOnEditorActionListener  true
                }else{
                    return@setOnEditorActionListener false
                }
            }
        }
    }
    fun triggerOrderedListStyle(stop : Boolean){
        if (stop){
            setOnEditorActionListener(null)
        }else{
            var currentNum = 1
            if (text!!.isNotEmpty()){
                if (text.toString().substring(text!!.length - 2, text!!.length) != "\n"){
                    text!!.append("\n  ")
                }else{
                    text!!.append("  ")
                }
            }else{
                text!!.append("  ")
            }

            text!!.setSpan(OrderedListItemSpan(markwon.configuration().theme(), "${currentNum}-"), text!!.length - 2, text!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            currentNum ++

            setOnEditorActionListener { v, actionId, event ->

                if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN ))){
                    text!!.append("\n  ")
                    text!!.setSpan(OrderedListItemSpan(markwon.configuration().theme(), "${currentNum}-"), text!!.length - 2, text!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    currentNum ++
                    return@setOnEditorActionListener  true
                }else{
                    return@setOnEditorActionListener false
                }
            }
        }
    }


    private fun styliseText(
        textStyle: TextStyle, s: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) {
        when (textStyle) {
            TextStyle.BOLD -> {
                if (before < count) {
                        text!!.setSpan(
                            StrongEmphasisSpan(),
                            start,
                            start + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                }
            }
            TextStyle.ITALIC -> {
                if (before < count) {
                        text!!.setSpan(
                            EmphasisSpan(),
                            start,
                            start + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                }
            }
            TextStyle.STRIKE -> {
                    text!!.setSpan(
                        StrikethroughSpan(),
                        start,
                        start + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

            }

            else -> {
            }
        }


    }

    enum class TextStyle {
        BOLD,
        ITALIC,
        STRIKE,
        QUOTE,
        UNORDERED_LIST,
        ORDERED_LIST
    }

    fun getMD(): String {
        var mdText = text
        val startList = emptyList<Int>().toMutableList()
        val endList = emptyList<Int>().toMutableList()
        var i = 0

        for ((index, span) in text!!.getGivenSpans(span = *arrayOf(TextStyle.BOLD, TextStyle.ITALIC, TextStyle.STRIKE, TextStyle.QUOTE, TextStyle.UNORDERED_LIST, TextStyle.ORDERED_LIST)).withIndex()) {
            val start = text!!.getSpanStart(span)
            val end = text!!.getSpanEnd(span)
            startList.add(index, start)
            endList.add(index, end)
        }

        for ((index, start) in startList.sorted().withIndex()) {
            val end = endList.sorted()[index]
            val spannedText = end.let { text!!.substring(start, it) }
            val span = end.let { text!!.getSpans(start, it, Any::class.java) }
            if (span != null) {
                for (selectedSpan in span.distinctBy { it.javaClass }) {
                    if (spannedText.length > 1) {
                        when (selectedSpan) {
                            is StrongEmphasisSpan -> {
                                val mdString = "**$spannedText**"
                                mdText = SpannableStringBuilder(mdText!!.replaceRange(start + i, end + i, mdString))
                                i += 4
                            }
                            is EmphasisSpan -> {
                                val mdString = "_${spannedText}_"
                                mdText = SpannableStringBuilder(mdText!!.replaceRange(start + i, end + i, mdString))
                                i += 2
                            }
                            is StrikethroughSpan -> {
                                val mdString = "~~$spannedText~~"
                                mdText = SpannableStringBuilder(mdText!!.replaceRange(start + i, end + i, mdString))
                                i += 4
                            }
                            is QuoteSpan -> {
                                val mdString = ">$spannedText"
                                mdText = SpannableStringBuilder(mdText!!.replaceRange(start + i, end + i, mdString))
                                i += 1
                            }
                            is BulletListItemSpan -> {
                                val mdString = "* $spannedText"
                                mdText = SpannableStringBuilder(mdText!!.replaceRange(start + i, end + i, mdString))
                                i += 2
                            }

                            is OrderedListItemSpan -> {

                                val mdString = "* $spannedText"
                                mdText = SpannableStringBuilder(mdText!!.replaceRange(start + i, end + i, mdString))
                                i += 2
                            }
                        }
                    }
                }
            }

        }
        return mdText.toString()
    }


    private  fun Editable.getGivenSpans(vararg span : TextStyle): MutableList<Any>{
        val spanList = emptyArray<Any>().toMutableList()
        for (selectedSpan in span){
            when(selectedSpan){
                TextStyle.BOLD -> {
                    this.getSpans<StrongEmphasisSpan>().forEach {
                        spanList.add(it)
                    }
                }
                TextStyle.ITALIC -> {
                    this.getSpans<EmphasisSpan>().forEach {
                        spanList.add(it)
                    }
                }
                TextStyle.STRIKE -> {
                    this.getSpans<StrikethroughSpan>().forEach {
                        spanList.add(it)
                    }
                }
                TextStyle.QUOTE -> {
                    this.getSpans<QuoteSpan>().forEach {
                        spanList.add(it)
                    }
                }
                TextStyle.UNORDERED_LIST ->  {
                    this.getSpans<BulletListItemSpan>().forEach {
                        spanList.add(it)
                    }
                }
                TextStyle.ORDERED_LIST ->  {
                    this.getSpans<OrderedListItemSpan>().forEach {
                        spanList.add(it)
                    }
                }
            }
        }
        return spanList
    }
}