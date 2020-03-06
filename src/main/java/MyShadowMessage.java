import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import listener.OnMessageListener;
import listener.OnSuccessListener;

public class MyShadowMessage extends AWSIotMessage {
    private OnMessageListener listener;

    public void setListener(OnMessageListener listener){
        this.listener = listener;
    }
    public MyShadowMessage(){
        super(null,null);
    }
    public MyShadowMessage(String topic, AWSIotQos qos) {
        super(topic, qos);
    }

    public MyShadowMessage(String topic, AWSIotQos qos, byte[] payload) {
        super(topic, qos, payload);
    }

    public MyShadowMessage(String topic, AWSIotQos qos, String payload) {
        super(topic, qos, payload);
    }

    @Override
    public void onSuccess() {

        super.onSuccess();
        listener.onSuccess();
    }

    @Override
    public void onFailure() {
        listener.onFailure();
        super.onFailure();
    }

    @Override
    public void onTimeout() {
        listener.onTimeout();
        super.onTimeout();
    }
}
