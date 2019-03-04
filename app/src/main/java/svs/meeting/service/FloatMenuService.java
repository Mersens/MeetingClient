package svs.meeting.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import svs.meeting.activity.SignInShowActivity;
import svs.meeting.app.MainActivity;
import svs.meeting.app.R;
import svs.meeting.data.IntentType;
import svs.meeting.util.Helper;
import svs.meeting.widgets.FloatManager;


public class FloatMenuService extends Service {
	FloatManager floatManager;
	Context mActivity;
	public static final int COMPERE_TYPE=1;
	public static final int CLIENT_TYPE=2;
	private int type=0;
	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent.hasExtra("type")){
			type=intent.getIntExtra("type",0);
		}

		initMenu();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mActivity=getApplicationContext();

	}

	private void initMenu() {
		if (floatManager != null) return;
		floatManager = new FloatManager(getApplicationContext(), new FloatManager.GetViewCallback() {
			@Override
			public View getLeftView(View.OnTouchListener touchListener) {
				View v=LayoutInflater.from(mActivity).inflate(R.layout.layout_menu_r,null);
				v.setOnTouchListener(touchListener);
				initLeftViewEvent(v);
				return v;
			}


			@Override
			public View getRightView(View.OnTouchListener touchListener) {
				View v=LayoutInflater.from(mActivity).inflate(R.layout.layout_menu_l,null);
				v.setOnTouchListener(touchListener);
				initRightViewEvent(v);
				return v;
			}

			@Override
			public View getLogoView() {
				return LayoutInflater.from(mActivity).inflate(R.layout.layout_float_logo,null);
			}

			@Override
			public void resetLogoViewSize(int hintLocation, View logoView) {
				logoView.setTranslationX(0);
				logoView.setScaleX(1);
				logoView.setScaleY(1);
			}

			@Override
			public void dragingLogoViewOffset(final View smallView, boolean isDraging, boolean isResetPosition, float offset) {
				/*if (isDraging && offset > 0) {
					smallView.setScaleX(1 + offset);
					smallView.setScaleY(1 + offset);
				} else {
					smallView.setTranslationX(0);
					smallView.setScaleX(1);
					smallView.setScaleY(1);
				}*/

			}

			@Override
			public void shrinkLeftLogoView(View smallView) {
				smallView.setTranslationX(-smallView.getWidth() / 3);
			}

			@Override
			public void shrinkRightLogoView(View smallView) {
				smallView.setTranslationX(smallView.getWidth() / 3);
			}

			@Override
			public void leftViewOpened(View leftView) {
			}

			@Override
			public void rightViewOpened(View rightView) {
			}

			@Override
			public void leftOrRightViewClosed(View smallView) {
			}

			@Override
			public void onDestoryed() {

			}
		});
		floatManager.show();

	}
	private void initLeftViewEvent(View view){
		RelativeLayout layout_tpgx=view.findViewById(R.id.layout_tpgx);
		layout_tpgx.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.TPGX);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
		RelativeLayout layout_wbsp=view.findViewById(R.id.layout_wbsp);
		layout_wbsp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.WBSP);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
		RelativeLayout layout_hyzl=view.findViewById(R.id.layout_hyzl);
		layout_hyzl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.HYZL);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
		RelativeLayout layou_hjfw=view.findViewById(R.id.layou_hjfw);
		layou_hjfw.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.HJFW);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
		RelativeLayout layout_jsq=view.findViewById(R.id.layout_jsq);
		layout_jsq.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.JSQ);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
		RelativeLayout layout_fhhy=view.findViewById(R.id.layout_fhhy);
		layout_fhhy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.FHHY);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});

	}
	private void initRightViewEvent(View view){
		RelativeLayout layout_tpgx=view.findViewById(R.id.layout_tpgx);
		layout_tpgx.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.TPGX);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
		RelativeLayout layout_wbsp=view.findViewById(R.id.layout_wbsp);
		layout_wbsp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.WBSP);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
		RelativeLayout layout_hyzl=view.findViewById(R.id.layout_hyzl);
		layout_hyzl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.HYZL);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
		RelativeLayout layou_hjfw=view.findViewById(R.id.layou_hjfw);
		layou_hjfw.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.HJFW);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
		RelativeLayout layout_jsq=view.findViewById(R.id.layout_jsq);
		layout_jsq.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.JSQ);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
		RelativeLayout layout_fhhy=view.findViewById(R.id.layout_fhhy);
		layout_fhhy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(FloatMenuService.this, MainActivity.class);
				intent.putExtra("type",IntentType.FHHY);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
			}
		});
	}
	@Override
	public void onDestroy() {
		if(floatManager!=null){
			floatManager.destoryFloat();
		}
		super.onDestroy();
		
	}
	

}
