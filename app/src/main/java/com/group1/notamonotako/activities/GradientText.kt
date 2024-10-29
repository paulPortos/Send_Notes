package com.group1.notamonotako.activities

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.group1.notamonotako.R

class GradientText {
    companion object {
        fun setGradientText(view: View, context: Context) {
            val colors = intArrayOf(
                context.getColor(R.color.colorStart),
                context.getColor(R.color.colorEnd)
            ) // Define your gradient colors
            val shader = LinearGradient(
                0f,
                0f,
                0f,
                view.height.toFloat(),
                colors,
                null,
                Shader.TileMode.CLAMP
            )
            when (view) {
                is TextView -> {
                    view.paint.shader = shader
                }
                is AppCompatButton -> {
                    view.paint.shader = shader
                }
            }
            view.invalidate()
        }
    }
}