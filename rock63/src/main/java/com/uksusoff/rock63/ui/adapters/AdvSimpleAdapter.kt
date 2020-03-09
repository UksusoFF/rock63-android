package com.uksusoff.rock63.ui.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.*

/**
 * Based on SimpleAdapter source from api 18
 *
 */
class AdvSimpleAdapter(
        context: Context,
        private var data: List<Map<String, *>>,
        private var dropDownResource: Int,
        val from: Array<String>,
        val to: IntArray
) : BaseAdapter(), Filterable {
    private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val filter: SimpleFilter by lazy {
        SimpleFilter()
    }
    private val unfilteredData: ArrayList<Map<String, *>> by lazy {
        ArrayList(data)
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, dropDownResource)
    }

    private fun createViewFromResource(position: Int, convertView: View?,
                                       parent: ViewGroup, resource: Int): View {
        val v: View = convertView ?: inflater.inflate(resource, parent, false)
        bindView(position, v)
        return v
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, dropDownResource)
    }

    private fun bindView(position: Int, view: View) {
        for (i in to.indices) {
            view.findViewById<View>(to[i]).let { v ->
                val data = data[position][from[i]]
                val text = data?.toString() ?: ""

                (v as? Checkable)?.let { checkable ->
                    (data as? Boolean)?.let { checked ->
                        checkable.isChecked = checked
                    } ?: (v as? TextView)?.let {
                        // Note: keep the instanceof TextView check at the bottom of these
                        // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                        it.text = text
                    } ?: run {
                        throw IllegalStateException(
                                "${v.javaClass.name} should be bound to a Boolean, not a " +
                                (data?.javaClass ?: "<unknown type>")
                        )
                    }
                } ?: (v as? TextView)?.let {
                    // Note: keep the instanceof TextView check at the bottom of these
                    // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                    it.text = text
                } ?: (v as? ImageView)?.let {
                    (data as? Int)?.let { resId ->
                        setViewImage(it, resId)
                    } ?: run {
                        setViewImage(it, text)
                    }
                } ?: run {
                    throw IllegalStateException(
                            "${v.javaClass.name} is not a " +
                            " view that can be bounds by this SimpleAdapter"
                    )
                }
            }
        }
    }

    private fun setViewImage(v: ImageView, resId: Int) {
        v.setImageResource(resId)
    }

    private fun setViewImage(v: ImageView, value: String) {
        try {
            setViewImage(v, value.toInt())
        } catch (nfe: NumberFormatException) {
            v.setImageURI(Uri.parse(value))
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    private inner class SimpleFilter : Filter() {
        override fun performFiltering(prefix: CharSequence): FilterResults {
            return if (prefix.isEmpty()) {
                unfilteredData
            } else {
                val contentString = prefix.toString().toLowerCase(Locale.ROOT)
                unfilteredData.filter { h ->
                    from.any { key ->
                        (h[key] as String)
                                .toLowerCase(Locale.ROOT)
                                .contains(contentString)
                    }
                }
            }.let { newValues ->
                FilterResults().apply {
                    this.values = newValues
                    this.count = newValues.size
                }
            }
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            if (results.count > 0) {
                @Suppress("UNCHECKED_CAST")
                data = results.values as List<Map<String, *>>
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }
}