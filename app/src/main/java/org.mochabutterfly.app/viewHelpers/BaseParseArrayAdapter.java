package org.mochabutterfly.app.viewHelpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;

/**
 * Helper class that makes it easier to work with Parse ListViews. Also optimizes list view management via the View Holder pattern.
 * Simply subclass {@link #initView(ViewHolder holder, T data)} and instantiate your views using the provided {@link ViewHolder}.
 *
 * @author Usama
 *         <p/>
 */
public class BaseParseArrayAdapter<T extends ParseObject> extends ParseQueryAdapter<T>
{
    private int rowLayoutResource;

    public BaseParseArrayAdapter(Context context, QueryFactory query, int itemViewResource)
    {
        super(context, query, itemViewResource);
        this.rowLayoutResource = itemViewResource;
    }

    @Override
    public View getItemView(T object, View convertView, ViewGroup parent)
    {
        View row = convertView;
        if (row == null)
        {
            row = LayoutInflater.from(getContext()).inflate(rowLayoutResource, parent, false);

            ViewHolder holder = new ViewHolder();
            ArrayList<View> childViews = getAllChildrenForRootView(row);
            for (View view : childViews)
            {
                holder.addView(view);
            }

            row.setTag(holder);
        }

        initView((ViewHolder) row.getTag(), object);
        return row;
    }

    protected void initView(ViewHolder holder, T data)
    {
    }

    public static ArrayList<View> getAllChildrenForRootView(View rootView)
    {
        try
        {
            ViewGroup viewGroup = (ViewGroup) rootView;

            ArrayList<View> output = new ArrayList<View>();
            int childCount = viewGroup.getChildCount();
            output.add(viewGroup); // Keep track of views that have children

            for (int i = 0; i < childCount; i++)
            {
                View child = viewGroup.getChildAt(i);
                ArrayList<View> viewsForChild = getAllChildrenForRootView(child);
                output.addAll(viewsForChild);
            }

            return output;
        }
        catch (ClassCastException e) // View does not have any children
        {
            ArrayList<View> child = new ArrayList<View>();
            child.add(rootView);
            return child;
        }
    }

    public int getRowLayoutResource()
    {
        return rowLayoutResource;
    }

    public void setRowLayoutResource(int rowLayoutResource)
    {
        this.rowLayoutResource = rowLayoutResource;
    }
}
