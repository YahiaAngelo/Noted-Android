package com.noted.noted.view.customView

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.MetricAffectingSpan
import android.util.AttributeSet
import androidx.core.text.getSpans
import com.google.android.material.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap
import io.noties.markwon.Markwon
import io.noties.markwon.core.spans.EmphasisSpan
import io.noties.markwon.core.spans.StrongEmphasisSpan

class MarkdownEditText : TextInputEditText {

    var markwon: Markwon
    var mListeners: ArrayList<TextWatcher>? = null
    private var textWatcher: TextWatcher? = null

    constructor(context: Context) : super(context, null) {
        markwon = Markwon.create(context)
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs,
        R.attr.editTextStyle
    ) {
        markwon = Markwon.create(context)
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        wrap(
            context,
            attrs,
            defStyleAttr,
            0
        ), attrs, defStyleAttr
    ) {
        markwon = Markwon.create(context)
        init()
    }


    private fun init() {

        /* val editor = MarkwonEditor.create(markwon)
         addTextChangedListener(MarkwonEditorTextWatcher.withPreRender(
             editor,
             Executors.newCachedThreadPool(),
             this
         ))

         */
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

    private fun styliseText(
        textStyle: TextStyle, s: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) {
        when (textStyle) {
            TextStyle.BOLD -> {
                if (before < count) {
                    val spanned = markwon.toMarkdown("**$s**")
                    val span = spanned.getSpans(0, spanned.length, Any::class.java)
                    if (span.isNotEmpty()) {
                        text!!.setSpan(
                            span[0],
                            start,
                            start + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
            TextStyle.ITALIC -> {
                if (before < count) {
                    val spanned = markwon.toMarkdown("_${s}_")
                    val span = spanned.getSpans(0, spanned.length, Any::class.java)
                    if (span.isNotEmpty()) {
                        text!!.setSpan(
                            span[0],
                            start,
                            start + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }

            }

            else -> {
            }
        }


    }

    enum class TextStyle {
        BOLD,
        ITALIC
    }

    fun getMD(): String {
        var mdText = text
        val startList = emptyList<Int>().toMutableList()
        val endList = emptyList<Int>().toMutableList()
        var i = 0
        for ((index, span) in text!!.getSpans<MetricAffectingSpan>().withIndex()) {
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
                                mdText = SpannableStringBuilder(
                                    mdText!!.replaceRange(
                                        start + i,
                                        end + i,
                                        mdString
                                    )
                                )
                                i += 4
                            }
                            is EmphasisSpan -> {
                                val mdString = "_${spannedText}_"
                                mdText = SpannableStringBuilder(
                                    mdText!!.replaceRange(
                                        start + i,
                                        end + i,
                                        mdString
                                    )
                                )
                                i += 2
                            }
                        }
                    }
                }
            }

        }
        return mdText.toString()
    }


}