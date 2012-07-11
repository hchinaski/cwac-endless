package com.commonsware.cwac.endless;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import java.util.concurrent.atomic.AtomicBoolean;
import com.commonsware.cwac.adapter.AdapterWrapper;

public abstract class SyncEndlessAdapter extends AdapterWrapper
{
  protected abstract void onLoad();

  private View          pendingView     = null;
  private AtomicBoolean keepOnAppending = new AtomicBoolean(true);
  private Context       context;
  private int           pendingResource = -1;

  public SyncEndlessAdapter(ListAdapter wrapped)
  {
    super(wrapped);
  }

  public SyncEndlessAdapter(Context context, ListAdapter wrapped, int pendingResource)
  {
    super(wrapped);
    this.context = context;
    this.pendingResource = pendingResource;
  }

  private void setKeepLoading(boolean keep)
  {
    keepOnAppending.set(keep);
  }

  @Override
  public int getCount()
  {
    if (keepOnAppending.get())
    {
      return (super.getCount() + 1); // one more for "pending"
    }
    return (super.getCount());
  }

  public int getItemViewType(int position)
  {
    if (position == getWrappedAdapter().getCount())
    {
      return (IGNORE_ITEM_VIEW_TYPE);
    }

    return (super.getItemViewType(position));
  }

  public int getViewTypeCount()
  {
    return (super.getViewTypeCount() + 1);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    if (position == super.getCount() && keepOnAppending.get())
    {
      if (pendingView == null)
      {
        pendingView = getPendingView(parent);
        onLoad();
      }

      if (pendingView != null)
        return (pendingView);
      else
        return new View(context);
    }

    return (super.getView(position, convertView, parent));
  }

  public void onLoadFinished(boolean keepLoading)
  {
    setKeepLoading(keepLoading);
    pendingView = null;
    notifyDataSetChanged();
  }

  /**
   * Inflates pending view using the pendingResource ID passed into the constructor
   * 
   * @param parent
   * @return inflated pending view, or null if the context passed into the pending view constructor was null.
   */
  protected View getPendingView(ViewGroup parent)
  {
    if (context != null)
    {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      return inflater.inflate(pendingResource, parent, false);
    }

    throw new RuntimeException("You must either override getPendingView() or supply a pending View resource via the constructor");
  }

  /**
   * Getter method for the Context being held by the adapter
   * 
   * @return Context
   */
  protected Context getContext()
  {
    return (context);
  }
}