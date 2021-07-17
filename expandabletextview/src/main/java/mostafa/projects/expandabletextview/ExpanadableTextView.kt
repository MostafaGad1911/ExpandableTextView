package mostafa.projects.expandabletextview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

class ExpandableTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0) :
    AppCompatTextView(context, attrs, def) {

    private var underlineColor = Color.BLUE
    private var underlineMaxlines = 0
    private var isViewMore = true

    init {
        Companion.context = context
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.expandableview,
            0, 0
        )
        underlineColor = typedArray.getColor(R.styleable.expandableview_underlineColor, Color.parseColor("#82B1FF"))
        underlineMaxlines = typedArray.getInt(R.styleable.expandableview_underlineMaxlines, 0)
        initViews(typedArray)
        typedArray.recycle()

    }

    fun initViews(a:TypedArray) {
        if (this.text.toString().isEmpty()) {
            return
        }
        if (this.tag == null) {
            this.tag = this.text
        }
        val vto = this.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val text: String
                var expandText = "${this@ExpandableTextView.context.getString(R.string.see)} "
                val lineEndIndex: Int
                val obs = this@ExpandableTextView.viewTreeObserver
                obs.removeGlobalOnLayoutListener(this)
                val lineCount = this@ExpandableTextView.layout.lineCount
                expandText += if (isViewMore) "${context.getString(R.string.s_more)}" else "${
                    context.getString(
                        R.string.s_less
                    )
                }"
                if (lineCount <= underlineMaxlines) {
                    lineEndIndex = this@ExpandableTextView.layout.getLineEnd(this@ExpandableTextView.layout.lineCount - 1)
                    text = this@ExpandableTextView.text.subSequence(0, lineEndIndex).toString()
                } else if (isViewMore && underlineMaxlines > 0 && this@ExpandableTextView.lineCount >= underlineMaxlines) {
                    lineEndIndex = this@ExpandableTextView.layout.getLineEnd(underlineMaxlines - 1)
                    text = this@ExpandableTextView.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                } else {
                    lineEndIndex = this@ExpandableTextView.layout.getLineEnd(this@ExpandableTextView.layout.lineCount - 1)
                    text = this@ExpandableTextView.text.subSequence(0, lineEndIndex).toString() + " " + expandText
                }
                this@ExpandableTextView.text = text
                this@ExpandableTextView.movementMethod = LinkMovementMethod.getInstance()
                if (lineCount > underlineMaxlines) this@ExpandableTextView.setText(
                    addClickablePartTextViewResizable(expandText , a),
                    BufferType.SPANNABLE
                )
                this@ExpandableTextView.isSelected = true
            }
        })
    }

    fun addClickablePartTextViewResizable(expandText: String , a:TypedArray): SpannableStringBuilder {
        val string = this.text.toString()
        val expandedStringBuilder = SpannableStringBuilder(string)
        if (string.contains(expandText)) {
            expandedStringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    this@ExpandableTextView.layoutParams = this@ExpandableTextView.layoutParams
                    this@ExpandableTextView.setText(this@ExpandableTextView.tag.toString(), BufferType.SPANNABLE)
                    this@ExpandableTextView.invalidate()
//                    underlineMaxlines = if (isViewMore) -1 else this@ExpandableTextView.lineCount
                    isViewMore = !isViewMore
                    initViews(a)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = true
                    ds.color = underlineColor
                }
            }, string.indexOf(expandText), string.length, 0)
        }
        return expandedStringBuilder
    }

    companion object {
        private var context: Context? = null
    }
}