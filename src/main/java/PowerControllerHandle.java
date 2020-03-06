import bean.Desired;
import bean.State;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import org.json.JSONObject;

public class PowerControllerHandle {
    private static PowerControllerHandle handle;
    private AWSIotDevice device;
    private AlexaResponse ar;

    public static PowerControllerHandle getInstance() {
        if (handle == null){
            handle = new PowerControllerHandle();
        }
        return handle;
    }

    public AlexaResponse doResponse(JSONObject directive,String correlationToken){
        JSONObject powerEndpoint = (JSONObject) directive.get("endpoint");
        //设置endpointId为thingName
        String powerEndpointId = directive.getJSONObject("endpoint").optString("endpointId", "INVALID");
        String powerToken = directive.getJSONObject("endpoint").getJSONObject("scope").optString("token", "INVALID");
        String powerStateValue = directive.getJSONObject("header").optString("name", "TurnOn");
        String powerValue = powerStateValue.equals("TurnOn") ? "on" : "off";

        device = MqttClient.getClient().initClient(powerEndpoint,powerEndpointId);

        //AWSIotQos qos = AWSIotQos.QOS0;
        long timeout = 5000;// milliseconds
        State powerState = new State(new Desired("light",powerValue));
        System.out.println("DATA:"+powerState.toDesiredString());

        //String data = "{ \"state\":{ \"desired\": { \"connect\": true }}}";
        String topicName = "$aws/things/"+powerEndpointId+"/shadow/update/accept";
        //更新
        try {
            device.update(powerState.toDesiredString());
        } catch (AWSIotException e) {
            e.printStackTrace();
        }
        ar = new AlexaResponse<String>("Alexa", "Response", powerEndpointId, powerToken, correlationToken
                ,false);
        ar.AddContextProperty("Alexa.PowerController", "powerState", powerValue, 500);
        MqttClient.getClient().closeClient();
        //订阅
//                    MyTopic myTopic = new MyTopic(topicName, qos);
//                    client.subscribe(myTopic);

        //获取
//                    String state = device.get(timeout);
//                    System.out.println("STATE:"+state);

        //更新
//                    MyShadowMessage getMessage = new MyShadowMessage(topicName,qos,state.toString());
//                    device.update(getMessage,timeout);

        //发布
//                    MyShadowMessage message = new MyShadowMessage(topicName, qos, state.toString());
//                    client.publish(message, timeout);
        return ar;
    }
}
