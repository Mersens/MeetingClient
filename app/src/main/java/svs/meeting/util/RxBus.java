package svs.meeting.util;


import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import svs.meeting.data.EventEntity;

/**
 * Created by Mersens on 2017/5/1
 * Email:626168564@qq.com
 */

public class RxBus {

    //PublishSubject可以先订阅事件，然后在某一时刻通过调用去触发响应
    private static final FlowableProcessor<Object> bus =  PublishProcessor.create().toSerialized();
    private static RxBus rxBus;
    private RxBus(){

    }
    //获取实例
    public static RxBus getInstance() {
        if (rxBus == null) {
            synchronized (RxBus.class){
                if(rxBus == null){
                    rxBus = new RxBus();
                }
            }
        }
        return rxBus;
    }
    //发送数据
    public void send(EventEntity obj) {
        bus.onNext(obj);
    }

    //监听订阅事件
    public Flowable<Object> toObservable() {
        return bus;
    }
}
