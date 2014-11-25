package com.devotify.gabrielhorn.viewHelpers;

import android.view.View;

import java.util.HashMap;

/**
 * @author Usama
 *         This class should be subclassed when using custom row layouts for list views. Using a view holder significantly optimizes list view
 *         loading because {@link #findViewById()} is not called for each row in the layout after initialization has been completed. Elements in the list view
 *         are stored in a hashmap, where an int is the id of the requested UI element within the row and also functions as a key that returns the
 *         cached view.
 */
public class ViewHolder
{
    private HashMap<Integer, View> storedViews = new HashMap<Integer, View>();

    public ViewHolder()
    {
    }

    /**
     * @param view The view to add; to reference this view later, simply refer to its id.
     * @return This instance to allow for chaining.
     */
    public ViewHolder addView(View view)
    {
        int id = view.getId();
        storedViews.put(id, view);
        return this;
    }

    public View getView(int id)
    {
        return storedViews.get(id);
    }
}