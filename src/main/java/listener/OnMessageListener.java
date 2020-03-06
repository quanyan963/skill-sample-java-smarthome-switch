package listener;

public interface OnMessageListener {
    void onSuccess();
    void onFailure();
    void onTimeout();
}
