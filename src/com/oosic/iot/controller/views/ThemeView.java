package com.oosic.iot.controller.views;

import com.oosic.iot.controller.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ThemeView extends FrameLayout {

   private Context context;
   private ViewGroup layout;
   private ImageView icoView;
   private TextView titleView;

   public ThemeView(Context context) {
      super(context);

      this.context = context;
      LayoutInflater inflater = LayoutInflater.from(context);
      this.layout = (ViewGroup) inflater.inflate(R.layout.theme_cell, this,
            true);
      findViews();
      relayoutViews();
   }

   private void findViews() {
      this.icoView = (ImageView) this.layout.findViewById(R.id.theme_ico);
      this.titleView = (TextView) this.layout.findViewById(R.id.theme_title);
   }

   private void relayoutViews() {

   }

   public ImageView getIcoView() {
      return this.icoView;
   }

   public TextView getTitleView() {
      return this.titleView;
   }

}
