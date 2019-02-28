package svs.meeting.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class PaletteUtils {
    public static final int DEFAULT_STROKE_SIZE = 3;
    public static Paint getDefaultPaint(){
        Paint strokePaint=new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setDither(true);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(DEFAULT_STROKE_SIZE);
        return strokePaint;
    }

    public static JSONObject getPointJson(int x,int y){
        JSONObject object=new JSONObject();
        try {
            object.put("x",x+"");
            object.put("y",y+"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static String bitmapToBase64(Bitmap bitmap){
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,
                100, baos2);
        byte[] bytes = baos2.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
