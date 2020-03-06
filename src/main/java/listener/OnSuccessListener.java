package listener;

public interface OnSuccessListener {
    void onSuccess();
    void onFailure();
    void onTimeout();
    void onMessage();
}
