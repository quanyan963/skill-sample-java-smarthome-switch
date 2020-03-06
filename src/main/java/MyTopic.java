import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import listener.OnSuccessListener;

public class MyTopic extends AWSIotTopic {
    private OnSuccessListener listener;
    public MyTopic(String topic) {
        super(topic);
    }

    public MyTopic(String topic, AWSIotQos qos) {
        super(topic, qos);
    }

    public void setListener(OnSuccessListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSuccess() {
        listener.onSuccess();
        super.onSuccess();
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        System.out.println("TopicMessagePayload"+message.getStringPayload());
        System.out.println("TopicMessageTopic"+message.getTopic());
        listener.onMessage();
        super.onMessage(message);
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
