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
class AdvSimpleAdapter(context: Context, private var mData: List<Map<String, *>>,
                       private var mDropDownResource: Int, from: Array<String>, to: IntArray) : BaseAdapter(), Filterable {
    private val mTo: IntArray
    private val mFrom: Array<String>
    var viewBinder: ViewBinder? = null
    private val mResource: Int
    private val mInflater: LayoutInflater
    private var mFilter: SimpleFilter? = null
    private var mUnfilteredData: ArrayList<Map<String, *>>? = null
    /**
     * @see android.widget.Adapter.getCount
     */
    override fun getCount(): Int {
        return mData.size
    }

    /**
     * @see android.widget.Adapter.getItem
     */
    override fun getItem(position: Int): Any {
        return mData[position]
    }

    /**
     * @see android.widget.Adapter.getItemId
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * @see android.widget.Adapter.getView
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mResource)
    }

    private fun createViewFromResource(position: Int, convertView: View?,
                                       parent: ViewGroup, resource: Int): View {
        val v: View
        v = convertView ?: mInflater.inflate(resource, parent, false)
        bindView(position, v)
        return v
    }

    /**
     *
     * Sets the layout resource to create the drop down views.
     *
     * @param resource the layout resource defining the drop down views
     * @see .getDropDownView
     */
    fun setDropDownViewResource(resource: Int) {
        mDropDownResource = resource
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mDropDownResource)
    }

    private fun bindView(position: Int, view: View) {
        val dataSet = mData[position] ?: return
        val binder = viewBinder
        val from = mFrom
        val to = mTo
        val count = to.size
        for (i in 0 until count) {
            val v = view.findViewById<View>(to[i])
            if (v != null) {
                val data = dataSet[from[i]]
                var text = data?.toString() ?: ""
                if (text == null) {
                    text = ""
                }
                var bound = false
                if (binder != null) {
                    bound = binder.setViewValue(v, data, text)
                }
                if (!bound) {
                    if (v is Checkable) {
                        if (data is Boolean) {
                            (v as Checkable).isChecked = (data as Boolean?)!!
                        } else if (v is TextView) { // Note: keep the instanceof TextView check at the bottom of these
// ifs since a lot of views are TextViews (e.g. CheckBoxes).
                            setViewText(v as TextView, text)
                        } else {
                            throw IllegalStateException(v.javaClass.name +
                                    " should be bound to a Boolean, not a " +
                                    (data?.javaClass ?: "<unknown type>"))
                        }
                    } else if (v is TextView) { // Note: keep the instanceof TextView check at the bottom of these
// ifs since a lot of views are TextViews (e.g. CheckBoxes).
                        setViewText(v, text)
                    } else if (v is ImageView) {
                        if (data is Int) {
                            setViewImage(v, data)
                        } else {
                            setViewImage(v, text)
                        }
                    } else {
                        throw IllegalStateException(v.javaClass.name + " is not a " +
                                " view that can be bounds by this SimpleAdapter")
                    }
                }
            }
        }
    }

    /**
     * Called by bindView() to set the image for an ImageView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an ImageView.
     *
     * This method is called instead of [.setViewImage]
     * if the supplied data is an int or Integer.
     *
     * @param v ImageView to receive an image
     * @param value the value retrieved from the data set
     *
     * @see .setViewImage
     */
    fun setViewImage(v: ImageView, value: Int) {
        v.setImageResource(value)
    }

    /**
     * Called by bindView() to set the image for an ImageView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an ImageView.
     *
     * By default, the value will be treated as an image resource. If the
     * value cannot be used as an image resource, the value is used as an
     * image Uri.
     *
     * This method is called instead of [.setViewImage]
     * if the supplied data is not an int or Integer.
     *
     * @param v ImageView to receive an image
     * @param value the value retrieved from the data set
     *
     * @see .setViewImage
     */
    fun setViewImage(v: ImageView, value: String) {
        try {
            v.setImageResource(value.toInt())
        } catch (nfe: NumberFormatException) {
            v.setImageURI(Uri.parse(value))
        }
    }

    /**
     * Called by bindView() to set the text for a TextView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to a TextView.
     *
     * @param v TextView to receive text
     * @param text the text to be set for the TextView
     */
    fun setViewText(v: TextView, text: String?) {
        v.text = text
    }

    override fun getFilter(): Filter {
        if (mFilter == null) {
            mFilter = SimpleFilter()
        }
        return mFilter!!
    }

    /**
     * This class can be used by external clients of SimpleAdapter to bind
     * values to views.
     *
     * You should use this class to bind values to views that are not
     * directly supported by SimpleAdapter or to change the way binding
     * occurs for views supported by SimpleAdapter.
     *
     * @see SimpleAdapter.setViewImage
     * @see SimpleAdapter.setViewImage
     * @see SimpleAdapter.setViewText
     */
    interface ViewBinder {
        /**
         * Binds the specified data to the specified view.
         *
         * When binding is handled by this ViewBinder, this method must return true.
         * If this method returns false, SimpleAdapter will attempts to handle
         * the binding on its own.
         *
         * @param view the view to bind the data to
         * @param data the data to bind to the view
         * @param textRepresentation a safe String representation of the supplied data:
         * it is either the result of data.toString() or an empty String but it
         * is never null
         *
         * @return true if the data was bound to the view, false otherwise
         */
        fun setViewValue(view: View?, data: Any?, textRepresentation: String?): Boolean
    }

    /**
     *
     * An array filters constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.
     */
    private inner class SimpleFilter : Filter() {
        override fun performFiltering(prefix: CharSequence): FilterResults {
            val results = FilterResults()
            if (mUnfilteredData == null) {
                mUnfilteredData = ArrayList(mData)
            }
            if (prefix == null || prefix.length == 0) {
                val list = mUnfilteredData!!
                results.values = list
                results.count = list.size
            } else {
                val contentString = prefix.toString().toLowerCase()
                val unfilteredValues = mUnfilteredData!!
                val count = unfilteredValues.size
                val newValues = ArrayList<Map<String, *>>(count)
                for (i in 0 until count) {
                    val h = unfilteredValues[i]
                    if (h != null) {
                        val len = mTo.size
                        for (j in 0 until len) {
                            val str = h[mFrom[j]] as String?
                            if (str!!.toLowerCase().contains(contentString)) {
                                newValues.add(h)
                                break
                            }
                        }
                    }
                }
                results.values = newValues
                results.count = newValues.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            mData = results.values as List<Map<String, *>>
            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

    /**
     * Constructor
     *
     * @param context The context where the View associated with this SimpleAdapter is running
     * @param data A List of Maps. Each entry in the List corresponds to one row in the list. The
     * Maps contain the data for each row, and should include all the entries specified in
     * "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     * item. The layout file should include at least those named views defined in "to"
     * @param from A list of column names that will be added to the Map associated with each
     * item.
     * @param to The views that should display column in the "from" parameter. These should all be
     * TextViews. The first N views in this list are given the values of the first N columns
     * in the from parameter.
     */
    init {
        mResource = mDropDownResource
        mFrom = from
        mTo = to
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}