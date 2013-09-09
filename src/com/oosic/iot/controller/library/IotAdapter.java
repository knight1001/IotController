package com.oosic.iot.controller.library;

import java.util.List;

import android.content.Context;
import android.widget.BaseAdapter;

public abstract class IotAdapter<T> extends BaseAdapter {

   protected Context context;
   protected List<T> data;

   public IotAdapter(Context context) {
      this.context = context;
   }
   
   public IotAdapter(Context context, List<T> data) {
      this(context);
      this.data = data;
   }

   public void setData(List<T> data) {
      this.data = data;
   }

   @Override
   public int getCount() {
      if (this.data != null) {
         return this.data.size();
      }
      return 0;
   }

   @Override
   public Object getItem(int position) {
      if (this.data != null) {
         return this.data.get(position);
      }
      return null;
   }

   @Override
   public long getItemId(int position) {
      if (this.data != null) {
         return this.data.get(position).hashCode();
      }
      return 0;
   }

}
