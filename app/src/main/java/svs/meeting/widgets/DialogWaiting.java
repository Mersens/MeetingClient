package svs.meeting.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import svs.meeting.util.Helper;

public class DialogWaiting {
	private Context context;
	private PopupWindow popupWindow;
	private LayoutInflater inflater;
	private int width;
	private int height;
	private View view;
	private static org.json.JSONArray opts=null;
	
	private LinearLayout mainGrid=null;
	
	public DialogWaiting(Context _context){
		this.context=_context;
//		inflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//
//		view= (ViewGroup)inflater.inflate(R.layout.dialog_waiting, null);
//		view = LayoutInflater.from(_context).inflate(R.layout.dialog_waiting , null);
//
//		mainGrid=(LinearLayout)view.findViewById(R.id.mainGrid);
//		width= Helper.screenWidth;///3;
//		height=Helper.screenHeight;///2;
//		popupWindow = new PopupWindow(view, width, height);
//		popupWindow.setFocusable(false);
//		popupWindow.setOutsideTouchable(false);
//
//		ImageView gifImage = (ImageView) view.findViewById(R.id.loading_gif_image);
//		gifImage.setVisibility(View.GONE);
//		//		Glide.with(context).load(R.drawable.p801).into(gifImage);
//		try{
//			//*
//			GifImageView gib = new GifImageView(context );
//			gib.setImageResource( R.drawable.app_loading );
//
//			LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//			params.gravity=Gravity.CENTER_HORIZONTAL;
//			//params.setMargins(10, 0, 10, 15);
//			gib.setLayoutParams(params);
//			mainGrid.addView(gib);
//			//*/
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
	}
	public void showDialog(){
		final Activity activity = (Activity)context;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Helper.showDialog(activity);
			}
		});
//		showDialog("no title!");
	}

	public void showDialog(String strLabel){
		final Activity activity = (Activity)context;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Helper.showDialog(activity);
			}
		});
//		try {
//			if(popupWindow.isShowing())
//				return;
//			//popupWindow.setAnimationStyle(R.style.animation);
//			popupWindow.showAtLocation(view, Gravity.CENTER,0,0);
//			popupWindow.setOnDismissListener(new OnDismissListener(){
//
//				@Override
//				public void onDismiss() {
//					popupWindow.dismiss();
//
//				}});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}

	public void hideDialog02(){
//		popupWindow.dismiss();
		final Activity activity = (Activity)context;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Helper.dismisDialog();
			}
		});
	}

	public void hideDialog(){
		((Activity)context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				if(popupWindow.isShowing()){
//					new Handler().postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							popupWindow.setAnimationStyle(R.style.popwin_anim_style);
//							popupWindow.dismiss();
//						}
//					},300);
//				}
				Helper.dismisDialog();

			}
		});
	}
	
	public boolean isShowing(){
		return false;
	}
}
