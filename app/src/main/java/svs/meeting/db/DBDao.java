package svs.meeting.db;

import java.util.List;

import svs.meeting.data.MsgEntity;
import svs.meeting.data.User;

/**
 * Created by zzu on 2016/4/6.
 */
public interface DBDao {

    //查询全部消息
    public List<MsgEntity> findAllMsg();

    //查询全部消息
    public List<MsgEntity> findMsgById(String topic,String seat_no,String msg_type,String meeting_id);

    //删除单条信息
    public void delMsgById(String id);

    //删除全部信息
    public void delAllMsg();

    //添加信息
    public void addMsgInfo(MsgEntity entity);
}
