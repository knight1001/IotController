package com.oosic.iot.controller.views;

import com.oosic.iot.controller.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class LightView extends FrameLayout {

   private Context context;
   private ViewGroup layout;
   private TextView titleView;
   private TextView statusView;

   public LightView(Context context) {
      super(context);

      this.context = context;
      LayoutInflater inflater = LayoutInflater.from(context);
      this.layout = (ViewGroup) inflater.inflate(R.layout.light_cell, this,
            true);
   }

   private void findViews() {
      this.titleView = (TextView) this.layout
            .findViewById(R.id.light_cell_title);
      this.statusView = (TextView) this.layout
            .findViewById(R.id.light_cell_status);
   }

   private void relayoutViews() {

   }

   public TextView getTitleView() {
      return this.titleView;
   }

   public TextView getStatusView() {
      return this.statusView;
   }

}
