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

public class LightView extends FrameLayout {

   private Context context;
   private ViewGroup layout;
   private TextView titleView;
   private TextView statusView;
   private ImageView leftArrowView, rightArrowView;

   public LightView(Context context) {
      super(context);

      this.context = context;
      LayoutInflater inflater = LayoutInflater.from(context);
      this.layout = (ViewGroup) inflater.inflate(R.layout.light_cell, this,
            true);
      findViews();
      relayoutViews();
   }

   private void findViews() {
      this.titleView = (TextView) this.layout
            .findViewById(R.id.light_cell_title);
      this.statusView = (TextView) this.layout
            .findViewById(R.id.light_cell_status);
      this.leftArrowView = (ImageView) this.layout
            .findViewById(R.id.light_cell_arrow_l);
      this.rightArrowView = (ImageView) this.layout
            .findViewById(R.id.light_cell_arrow_r);
   }

   private void relayoutViews() {
      float scrWidth = (float) IotApp.getScreenWidth((Activity) this.context);
      float scrHeight = (float) IotApp.getScreenHeight((Activity) this.context);
      float wRatio = scrWidth / IotApp.DEFAULT_SCREEN_WIDTH;
      float hRatio = scrHeight / IotApp.DEFAULT_SCREEN_HEIGHT;
      float ratio = (Float.compare(wRatio, hRatio) < 0) ? wRatio : hRatio;

      Resources resources = this.context.getResources();
      ViewGroup rootLayout = (ViewGroup) this.layout.findViewById(R.id.light_cell_layout);
      if (rootLayout != null) {
         FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) rootLayout.getLayoutParams();
         params.height = (int) (resources.getDimension(R.dimen.light_cell_height) * hRatio);
         rootLayout.setLayoutParams(params);
      }
      if (this.titleView != null) {
         LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.titleView.getLayoutParams();
         params.width = (int) (resources.getDimension(R.dimen.light_cell_title_width) * wRatio);
         this.titleView.setLayoutParams(params);
      }
      if (this.statusView != null) {
         LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.statusView.getLayoutParams();
         params.width = (int) (resources.getDimension(R.dimen.light_cell_status_width) * wRatio);
         this.statusView.setLayoutParams(params);
      }
   }

   public TextView getTitleView() {
      return this.titleView;
   }

   public TextView getStatusView() {
      return this.statusView;
   }
   
   public ImageView getLeftArrowView() {
      return this.leftArrowView;
   }

   public ImageView getRightArrowView() {
      return this.rightArrowView;
   }
   
   @Override
   public void setSelected(boolean selected) {
      super.setSelected(selected);

      if (this.titleView != null) {
         this.titleView.setSelected(selected);
      }
      if (this.statusView != null) {
         this.statusView.setSelected(selected);
      }
      if (this.leftArrowView != null) {
         this.leftArrowView.setSelected(selected);
      }
      if (this.rightArrowView != null) {
         this.rightArrowView.setSelected(selected);
      }
   }

}
