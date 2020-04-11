package com.candyhouse.app.tabs.menu

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.candyhouse.R


data class BarMenuItem(
        val index: Int,
        val icon: Drawable?,
        val title: String
)

object ItemUtils {


    fun getCustomSamples(context: Context): List<BarMenuItem> {
        val samples = ArrayList<BarMenuItem>()
        samples.add(BarMenuItem(0,drawable(context, R.drawable.ic_icons_filled_add_friends), context.getString(R.string.add_contacts)))
        samples.add(BarMenuItem(1,drawable(context, R.drawable.ic_icons_filled_favorites), context.getString(R.string.new_sesame)))
        return samples
    }

    private fun drawable(context: Context, @DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(context, id)
    }
}