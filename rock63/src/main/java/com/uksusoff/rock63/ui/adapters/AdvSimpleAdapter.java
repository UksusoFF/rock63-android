package com.uksusoff.rock63.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Based on SimpleAdapter source from api 18
 */
public class AdvSimpleAdapter extends BaseAdapter implements Filterable {
    private final int[] mTo;
    private final String[] mFrom;
    private ViewBinder mViewBinder;

    private List<? extends Map<String, ?>> mData;

    private final int mResource;
    private int mDropDownResource;
    private final LayoutInflater mInflater;

    private SimpleFilter mFilter;
    private ArrayList<Map<String, ?>> mUnfilteredData;

    public AdvSimpleAdapter(
            Context context,
            List<? extends Map<String, ?>> data,
            int resource,
            String[] from,
            int[] to
    ) {
        mData = data;
        mResource = mDropDownResource = resource;
        mFrom = from;
        mTo = to;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mData.size();
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }

    private View createViewFromResource(
            int position,
            View convertView,
            ViewGroup parent,
            int resource
    ) {
        View v;
        if (convertView == null) {
            v = mInflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }

        bindView(position, v);

        return v;
    }

    public void setDropDownViewResource(int resource) {
        this.mDropDownResource = resource;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownResource);
    }

    private void bindView(int position, View view) {
        final Map dataSet = mData.get(position);
        if (dataSet == null) {
            return;
        }

        final ViewBinder binder = mViewBinder;
        final int[] to = mTo;
        final int count = to.length;

        for (int i = 0; i < count; i++) {
            final View v = view.findViewById(to[i]);
            if (v != null) {
                final Object data = dataSet.get(mFrom[i]);
                String text = data == null ? "" : data.toString();

                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, data, text);
                }

                if (!bound) {
                    if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked((Boolean) data);
                        } else if (v instanceof TextView) {
                            // Note: keep the instanceof TextView check at the bottom of these
                            // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                            setViewText((TextView) v, text);
                        } else {
                            throw new IllegalStateException(v.getClass().getName() +
                                    " should be bound to a Boolean, not a " +
                                    (data == null ? "<unknown type>" : data.getClass()));
                        }
                    } else if (v instanceof TextView) {
                        // Note: keep the instanceof TextView check at the bottom of these
                        // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        if (data instanceof Integer) {
                            setViewImage((ImageView) v, (Integer) data);
                        } else {
                            setViewImage((ImageView) v, text);
                        }
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }
    }

    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    public void setViewBinder(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
    }

    public void setViewImage(ImageView v, int value) {
        v.setImageResource(value);
    }

    public void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            v.setImageURI(Uri.parse(value));
        }
    }

    public void setViewText(TextView v, String text) {
        v.setText(text);
    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SimpleFilter();
        }
        return mFilter;
    }

    public interface ViewBinder {
        boolean setViewValue(View view, Object data, String textRepresentation);
    }

    private class SimpleFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<Map<String, ?>>(mData);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<Map<String, ?>> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String contentString = prefix.toString().toLowerCase();

                ArrayList<Map<String, ?>> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<Map<String, ?>> newValues = new ArrayList<Map<String, ?>>(count);

                for (int i = 0; i < count; i++) {
                    Map<String, ?> h = unfilteredValues.get(i);
                    if (h != null) {

                        int len = mTo.length;

                        for (int j = 0; j < len; j++) {
                            String str = (String) h.get(mFrom[j]);

                            if (str.toLowerCase().contains(contentString)) {
                                newValues.add(h);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mData = (List<Map<String, ?>>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
