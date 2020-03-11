package com.candyhouse.app.tabs.devices.ssm2.room

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.candyhouse.R
import com.candyhouse.sesame.db.Model.HistoryAndOperater
import org.zakariya.stickyheaders.SectioningAdapter
import java.text.SimpleDateFormat
import java.util.*


class SSMHistoryAdapter(var mTestGList: ArrayList<Pair<String, List<HistoryAndOperater>>>) : SectioningAdapter() {

    inner class ItemViewHolder(itemView: View) : SectioningAdapter.ItemViewHolder(itemView) {
        var name: TextView = itemView.findViewById<TextView>(R.id.name)
        var time: TextView = itemView.findViewById<TextView>(R.id.time)
        var head: ImageView = itemView.findViewById<ImageView>(R.id.head)
        var locktype: ImageView = itemView.findViewById<ImageView>(R.id.locktype)

    }

    inner class HeaderViewHolder(itemView: View) : SectioningAdapter.HeaderViewHolder(itemView) {
        var textView: TextView = itemView.findViewById<TextView>(R.id.title)
    }

    override fun getNumberOfSections(): Int {
        return mTestGList.size
    }

    override fun getNumberOfItemsInSection(sectionIndex: Int): Int {
        return mTestGList[sectionIndex].second.size
    }

    override fun doesSectionHaveHeader(sectionIndex: Int): Boolean {
        return getNumberOfSections() > 0
    }


    override fun onCreateHeaderViewHolder(parent: ViewGroup, headerType: Int): HeaderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v: View = inflater.inflate(R.layout.list_item_simple_header, parent, false)
        return HeaderViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindHeaderViewHolder(viewHolder: SectioningAdapter.HeaderViewHolder, sectionIndex: Int, headerType: Int) {
        val s = mTestGList[sectionIndex]
        val hvh = viewHolder as HeaderViewHolder
        hvh.textView.text = s.first
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, itemType: Int): SectioningAdapter.ItemViewHolder? {
        val inflater = LayoutInflater.from(parent.context)
        val v: View = inflater.inflate(R.layout.list_item_simple_item, parent, false)
        return ItemViewHolder(v)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindItemViewHolder(viewHolder: SectioningAdapter.ItemViewHolder, sectionIndex: Int, itemIndex: Int, itemType: Int) {
        val dataPair = mTestGList[sectionIndex]
        val ivh = viewHolder as ItemViewHolder
        val history = dataPair.second[itemIndex].history
        val operator = dataPair.second[itemIndex].opetator
        ivh.name.text = operator?.name ?: when (history.event) {
            "manual-operated" -> MainRoomFG.instance?.getString(R.string.manual_operated)
            "auto-lock" -> MainRoomFG.instance?.getString(R.string.autolock)
            else -> history.event
        }


        val time = SimpleDateFormat("HH:mm:ss a").format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").parse(history.timestamp))
        ivh.time.text = time
        ivh.locktype.setImageResource(if (history.locker) R.drawable.icon_lock else R.drawable.icon_unlock)


        if (history.event == "lock") {
            ivh.head.setImageDrawable(avatatImagGenaroter(operator?.firstname))
        } else {
            ivh.head.setImageResource(
                    when (history.event) {
                        "manual-operated" -> R.drawable.ic_handmove
                        else -> R.drawable.ic_autolock
                    }
            )
        }
    }


}

fun avatatImagGenaroter(name: String?): TextDrawable? {
    val drawable = TextDrawable.Builder()
            .setColor(ColorGenerator.DEFAULT.getColor(name ?: "NA"))
            .setShape(TextDrawable.SHAPE_ROUND)
            .setText(name ?: "NA")
            .build()
    return drawable
}