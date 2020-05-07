package com.noted.noted.view.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.noted.noted.R

class MarkdownStylesBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    lateinit var adapter: StylesBarAdapter
    var markdownEditText: MarkdownEditText? = null
    set(value) {adapter.markdownEditText = value}
    init {
        initView()
    }

    private fun initView() {

        val horizontalListView = HorizontalListView(context, null)
        horizontalListView.setDividerWidth(28)
        val styleButtons = ArrayList<StyleButton>()
        styleButtons.add(StyleButton(R.drawable.ic_format_bold, R.id.style_button_bold))
        styleButtons.add(StyleButton(R.drawable.ic_format_italic, R.id.style_button_italic))
        styleButtons.add(StyleButton(R.drawable.ic_format_strikethrough, R.id.style_button_strike))
        styleButtons.add(StyleButton(R.drawable.ic_format_quote, R.id.style_button_quote))
        styleButtons.add(StyleButton(R.drawable.ic_format_list_bulleted, R.id.style_button_unordered_list))
        styleButtons.add(StyleButton(R.drawable.ic_format_list_numbered, R.id.style_button_ordered_list))

        adapter = StylesBarAdapter(styleButtons, context,
            OnClickListener { v -> horizontalListView.selectMaterialButtonItem(v) })
        if (markdownEditText != null) {
            adapter.markdownEditText = markdownEditText
        }
        horizontalListView.adapter = adapter
        addView(horizontalListView)


    }



    class StylesBarAdapter(
        data: ArrayList<StyleButton>,
        context: Context,
       var onClickListener: OnClickListener
    ) : ArrayAdapter<StyleButton>(context, R.layout.styles_bar_item, data), View.OnClickListener {
        var markdownEditText: MarkdownEditText? = null

        override fun onClick(v: View?) {
            onClickListener.onClick(v)
            val button = v as MaterialButton

            if (markdownEditText != null) {
                when (button.id) {
                    R.id.style_button_bold -> markdownEditText!!.triggerStyle(
                        MarkdownEditText.TextStyle.BOLD,
                        !button.isChecked
                    )
                    R.id.style_button_italic -> markdownEditText!!.triggerStyle(
                        MarkdownEditText.TextStyle.ITALIC,
                        !button.isChecked
                    )
                    R.id.style_button_strike -> markdownEditText!!.triggerStyle(
                        MarkdownEditText.TextStyle.STRIKE,
                        !button.isChecked
                    )
                    R.id.style_button_quote -> markdownEditText!!.addQuote()
                    R.id.style_button_unordered_list -> markdownEditText!!.triggerUnOrderedListStyle(!button.isChecked)
                    R.id.style_button_ordered_list -> markdownEditText!!.triggerOrderedListStyle(!button.isChecked)
                }
            }
        }

        private var lastPosition = -1

        private open class ViewHolder(var materialButton: MaterialButton)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val styleButton = getItem(position)
            var result: View? = null
            var viewHolder: ViewHolder
            if (convertView == null) {
                val inflater = LayoutInflater.from(context)
                result = inflater.inflate(R.layout.styles_bar_item, parent, false)
                viewHolder = ViewHolder(result!!.findViewById(R.id.style_button))
                result.tag = viewHolder

            } else {
                viewHolder = (convertView.tag as? ViewHolder)!!
                result = convertView
            }
            lastPosition = position
            viewHolder.materialButton.icon = context.getDrawable(styleButton!!.icon)
            viewHolder.materialButton.id = styleButton.id
            viewHolder.materialButton.setOnClickListener(this)
            if (styleButton.id == R.id.style_button_quote){
                viewHolder.materialButton.isCheckable = false
            }
            viewHolder.materialButton.tag = position
            return result
        }
    }

    class StyleButton(var icon: Int, var id: Int)

}