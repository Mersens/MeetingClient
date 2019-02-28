package svs.meeting.util;

public interface IDownloadCallback {
	public void onDownloadStart(long total);
	public void onDownloadComplete();
	public void onDownloadProgress(int percent);
	public void onDownloadError();
}
