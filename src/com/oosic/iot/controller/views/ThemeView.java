package com.oosic.iot.controller.views;

import com.oosic.iot.controller.IotApp;
import com.oosic.iot.controller.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
      float scrWidth = (float) IotApp.getScreenWidth((Activity) this.context);
      float scrHeight = (float) IotApp.getScreenHeight((Activity) this.context);
      float wRatio = scrWidth / IotApp.DEFAULT_SCREEN_WIDTH;
      float hRatio = scrHeight / IotApp.DEFAULT_SCREEN_HEIGHT;
      float ratio = (Float.compare(wRatio, hRatio) < 0) ? wRatio : hRatio;

      Resources resources = this.context.getResources();
      if (this.icoView != null) {
         LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.icoView
               .getLayoutParams();
         params.width = (int) (resources
               .getDimension(R.dimen.theme_cell_ico_width) * ratio);
         params.height = (int) (resources
               .getDimension(R.dimen.theme_cell_ico_height) * ratio);
         this.icoView.setLayoutParams(params);
      }
   }

   public ImageView getIcoView() {
      return this.icoView;
   }

   public TextView getTitleView() {
      return this.titleView;
   }

   @Override
   public void setSelected(boolean selected) {
      super.setSelected(selected);

      if (this.icoView != null) {
         this.icoView.setSelected(selected);
      }
      if (this.titleView != null) {
         this.titleView.setSelected(selected);
      }
   }

}
