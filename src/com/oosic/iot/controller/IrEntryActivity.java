package com.oosic.iot.controller;

import java.util.LinkedList;

import com.oosic.iot.controller.R;
import com.oosic.iot.controller.utils.UIUtils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class IrEntryActivity extends IotBaseActivity implements OnClickListener {

   private static final String TAG = "IrEntryActivity";

   private ImageView mLeftView, mMiddleView, mRightView;
   private Handler mHandler = new Handler();
   private LinkedList<Integer> mNormalImages = new LinkedList<Integer>();
   private LinkedList<Integer> mHighlightImages = new LinkedList<Integer>();

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_ir_entry);

      findViews();
      init();
      mHandler.post(new Runnable() {
         public void run() {
            relayoutViews();
         }
      });
   }

   private void findViews() {
      mLeftView = (ImageView) findViewById(R.id.left_ico);
      mMiddleView = (ImageView) findViewById(R.id.middle_ico);
      mRightView = (ImageView) findViewById(R.id.right_ico);
   }

   private void init() {
      mNormalImages.addLast(R.drawable.ico_air_normal);
      mNormalImages.addLast(R.drawable.ico_tv_normal);
      mNormalImages.addLast(R.drawable.ico_sharebox_normal);

      mHighlightImages.addLast(R.drawable.ico_air_highlight);
      mHighlightImages.addLast(R.drawable.ico_tv_highlight);
      mHighlightImages.addLast(R.drawable.ico_sharebox_highlight);

      initViews();
   }

   private void initViews() {
      mLeftView.setTag(mNormalImages.get(0));
      mLeftView.setOnClickListener(this);
      mMiddleView.setTag(mHighlightImages.get(1));
      mMiddleView.setOnClickListener(this);
      mRightView.setTag(mNormalImages.get(2));
      mRightView.setOnClickListener(this);
   }

   private void relayoutViews() {
      float scrWidth = (float) IotApp.getScreenWidth(this);
      float scrHeight = (float) IotApp.getScreenHeight(this);
      float wRatio = scrWidth / IotApp.DEFAULT_SCREEN_WIDTH;
      float hRatio = scrHeight / IotApp.DEFAULT_SCREEN_HEIGHT;
      float ratio = (Float.compare(wRatio, hRatio) < 0) ? wRatio : hRatio;

      Resources resources = getResources();
      ImageView logoView = (ImageView) findViewById(R.id.light_logo);
      if (logoView != null) {
         RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) logoView
               .getLayoutParams();
         params.leftMargin = (int) (resources
               .getDimension(R.dimen.logo_margin_h) * wRatio);
         params.topMargin = (int) (resources
               .getDimension(R.dimen.logo_margin_v) * hRatio);
         logoView.setLayoutParams(params);
      }

   }

   private void scrollLeft() {
      mNormalImages.addFirst(mNormalImages.removeLast());
      mHighlightImages.addFirst(mHighlightImages.removeLast());

      refreshViews();
   }

   private void scrollRight() {
      mNormalImages.addLast(mNormalImages.removeFirst());
      mHighlightImages.addLast(mHighlightImages.removeFirst());

      refreshViews();
   }

   private void refreshViews() {
      mLeftView.setTag(mNormalImages.get(0));
      mLeftView.setImageResource(mNormalImages.get(0));
      mMiddleView.setTag(mHighlightImages.get(1));
      mMiddleView.setImageResource(mHighlightImages.get(1));
      mRightView.setTag(mNormalImages.get(2));
      mRightView.setImageResource(mNormalImages.get(2));
   }

   @Override
   public void onClick(View v) {
      Intent intent = null;
      int resId = (Integer) v.getTag();

      switch (resId) {
      case R.drawable.ico_air_normal:
      case R.drawable.ico_air_highlight:
         intent = new Intent();
         intent.setClass(this, AirControlActivity.class);
         break;
      case R.drawable.ico_tv_normal:
      case R.drawable.ico_tv_highlight:
         intent = new Intent();
         intent.setClass(this, TvControlActivity.class);
         break;
      case R.drawable.ico_sharebox_normal:
      case R.drawable.ico_sharebox_highlight:
         intent = new Intent();
         intent.setClass(this, ShareboxControlActivity.class);
         break;
      }

      if (intent != null) {
         try {
            startActivity(intent);
         } catch (ActivityNotFoundException e) {

         }
      }
   }

   @Override
   public boolean dispatchKeyEvent(KeyEvent event) {
      int key = event.getKeyCode();
      int action = event.getAction();

      if ((key == KeyEvent.KEYCODE_DPAD_LEFT)
            && (action == KeyEvent.ACTION_DOWN)) {
         scrollLeft();
      } else if ((key == KeyEvent.KEYCODE_DPAD_RIGHT)
            && (action == KeyEvent.ACTION_DOWN)) {
         scrollRight();
      }

      return super.dispatchKeyEvent(event);
   }

   @Override
   public void onResume() {
      super.onResume();
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
   }

   private void showToast(String msg) {
      UIUtils.showToast(this, msg);
   }

   private void showMessageDialog(String msg) {
      UIUtils.getAlertDialogBuilder(this).setMessage(msg)
            .setPositiveButton(R.string.ok, null).show();
   }

}
