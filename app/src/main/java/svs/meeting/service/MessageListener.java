package svs.meeting.service;

public interface MessageListener {
	public void onMessageArrived(String me);
	public void onConnectionEvent(String evtName);
}
