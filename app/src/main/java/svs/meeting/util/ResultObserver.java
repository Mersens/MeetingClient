package svs.meeting.util;

import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * Created by Mersens on 2017/6/19 17:30
 * Email:626168564@qq.com
 */

public class ResultObserver implements Observer<ResponseBody> {
    private RequestManager.onRequestCallBack callBack;
    public ResultObserver( RequestManager.onRequestCallBack callBack){
        this.callBack=callBack;
    }
    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }
    @Override
    public void onNext(@NonNull ResponseBody responseBody) {

        try {
            String res = responseBody.string();
            if (res == null) {
                callBack.onError("请求发生未知错误");
                return;
            }
            callBack.onSuccess(res);
        } catch (IOException e) {
            e.printStackTrace();
            callBack.onError(e.toString());
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {

        callBack.onError(e.toString());
    }

    @Override
    public void onComplete() {

    }
}
