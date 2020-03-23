import bean.Constants;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import org.json.JSONObject;

public class MqttClient {
    private static MqttClient mqttClient;
    private AWSIotDevice device;
    private AWSIotMqttClient client;

    public static MqttClient getClient() {
        if (mqttClient == null){
            mqttClient = new MqttClient();
        }
        return mqttClient;
    }

    public AWSIotDevice initClient(JSONObject endpoint, String thingName) {
        String clientEndpoint = "a311cdvk7hqtsk-ats.iot.us-east-1.amazonaws.com";       // replace <prefix> and <region> with your own
        String clientId = String.valueOf(System.currentTimeMillis());                              // replace with your own client ID. Use unique client IDs for concurrent connections.
// AWS IAM credentials could be retrieved from AWS Cognito, STS, or other secure sources
        client = new AWSIotMqttClient(clientEndpoint, clientId, Constants.ACCESS_KEY_ID, Constants.SECRET_KEY);

        device = new AWSIotDevice(thingName);
        try {
            client.attach(device);
            client.connect();
//            if (client.getConnectionStatus().equals(AWSIotConnectionStatus.DISCONNECTED)){
//                System.out.println("ConnectStatue:"+client.getConnectionStatus());
//                client.connect();
//            }
        } catch (AWSIotException e) {
            e.printStackTrace();
        }

        return device;
    }

    public void closeClient(){
        try {
            client.detach(device);
            client.disconnect(1000,false);
        } catch (AWSIotException e) {
            e.printStackTrace();
        } catch (AWSIotTimeoutException e) {
            e.printStackTrace();
        }

    }
}
