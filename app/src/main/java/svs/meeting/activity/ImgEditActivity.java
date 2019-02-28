package svs.meeting.activity;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import me.kareluo.imaging.IMGEditBaseActivity;
import me.kareluo.imaging.core.IMGMode;
import me.kareluo.imaging.core.IMGText;
import me.kareluo.imaging.core.file.IMGAssetFileDecoder;
import me.kareluo.imaging.core.file.IMGDecoder;
import me.kareluo.imaging.core.file.IMGFileDecoder;
import me.kareluo.imaging.core.util.IMGUtils;
import svs.meeting.data.Config;
import svs.meeting.util.ImageDownloader;

public class ImgEditActivity extends IMGEditBaseActivity {
    private static final int MAX_WIDTH = 1024;

    private static final int MAX_HEIGHT = 1024;
    public static final String EXTRA_IMAGE_SAVE_PATH = "IMAGE_SAVE_PATH";
    String url;
    @Override
    public Bitmap getBitmap() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }

        String path = intent.getStringExtra(EXTRA_IMAGE_SAVE_PATH);
        Log.e("path","path is "+Config.STOREPATH+path);
        if(TextUtils.isEmpty(path)){
            Log.e("path is null","path is null");
            return null;
        }


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;

        if (options.outWidth > MAX_WIDTH) {
            options.inSampleSize = IMGUtils.inSampleSize(Math.round(1f * options.outWidth / MAX_WIDTH));
        }

        if (options.outHeight > MAX_HEIGHT) {
            options.inSampleSize = Math.max(options.inSampleSize,
                    IMGUtils.inSampleSize(Math.round(1f * options.outHeight / MAX_HEIGHT)));
        }

         options.inJustDecodeBounds = false;
        Bitmap bitmap=BitmapFactory.decodeFile(Config.STOREPATH+path,options);
        if (bitmap == null) {
            Log.e("bitmap is null","bitmap is null");
            return null;
        }

        return bitmap;
    }
    @Override
    public void onText(IMGText text) {
        mImgView.addStickerText(text);
    }

    @Override
    public void onModeClick(IMGMode mode) {
        IMGMode cm = mImgView.getMode();
        if (cm == mode) {
            mode = IMGMode.NONE;
        }
        mImgView.setMode(mode);
        updateModeUI();

        if (mode == IMGMode.CLIP) {
            setOpDisplay(OP_CLIP);
        }
    }

    @Override
    public void onUndoClick() {
        IMGMode mode = mImgView.getMode();
        if (mode == IMGMode.DOODLE) {
            mImgView.undoDoodle();
        } else if (mode == IMGMode.MOSAIC) {
            mImgView.undoMosaic();
        }
    }

    @Override
    public void onCancelClick() {
        finish();
    }

    @Override
    public void onDoneClick() {
        String path1 = getIntent().getStringExtra(EXTRA_IMAGE_SAVE_PATH);
        if (!TextUtils.isEmpty(path1)) {
            Bitmap bitmap = mImgView.saveBitmap();
            if (bitmap != null) {
               String path=Config.STOREPATH+"/"+path1;
                FileOutputStream fout = null;
                try {
                    fout = new FileOutputStream(path);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fout);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fout != null) {
                        try {
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setResult(RESULT_OK);
                finish();
                return;
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onCancelClipClick() {
        mImgView.cancelClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onDoneClipClick() {
        mImgView.doClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onResetClipClick() {
        mImgView.resetClip();
    }

    @Override
    public void onRotateClipClick() {
        mImgView.doRotate();
    }

    @Override
    public void onColorChanged(int checkedColor) {
        mImgView.setPenColor(checkedColor);
    }

}
