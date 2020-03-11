package com.candyhouse.app.tabs.menu

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.candyhouse.R


data class BarMenuItem(
        val icon: Drawable?,
        val title: String
)

object ItemUtils {


    fun getCustomSamples(context: Context): List<BarMenuItem> {
        val samples = ArrayList<BarMenuItem>()
        samples.add(BarMenuItem(drawable(context, R.drawable.ic_icons_filled_add_friends), "Add Friend"))
        samples.add(BarMenuItem(drawable(context, R.drawable.ic_icons_filled_favorites), "New Sesame"))
        return samples
    }

    private fun drawable(context: Context, @DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(context, id)
    }
}