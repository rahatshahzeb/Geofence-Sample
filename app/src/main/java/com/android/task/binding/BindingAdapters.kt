package com.android.task.binding

import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.BindingAdapter
import com.android.task.R

object BindingAdapters {
    /**
     * Set the active and inactive states of button
     */
    @JvmStatic
    @BindingAdapter("geofence")
    fun setBtnDrawable(button: Button, isBtnActive: Boolean) {
        val resId: Int = if (isBtnActive) R.drawable.btn_active else  R.drawable.btn_inactive
        button.setBackgroundResource(resId)
    }

    /**
     * Attaches the [TextWatcher] with [EditText]
     */
    @JvmStatic
    @BindingAdapter("textChangedListener")
    fun bindTextWatcher(editText: AppCompatEditText, textWatcher: TextWatcher) {
        editText.addTextChangedListener(textWatcher)
    }

}