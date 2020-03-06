import bean.Percent;
import bean.State;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import org.json.JSONObject;

public class ModeControllerHandle {
    private static ModeControllerHandle handle;
    private AlexaResponse ar;
    private AWSIotDevice device;

    public static ModeControllerHandle getInstance() {
        if (handle == null){
            handle = new ModeControllerHandle();
        }
        return handle;
    }

    public AlexaResponse doReasponse(JSONObject directive,String correlationToken){
        JSONObject modeEndpoint = (JSONObject) directive.get("endpoint");
        //设置endpointId为thingName
        String modeEndpointId = directive.getJSONObject("endpoint").optString("endpointId", "INVALID");
        String modeToken = directive.getJSONObject("endpoint").getJSONObject("scope").optString("token", "INVALID");
        String modeStateValue = directive.getJSONObject("header").optString("name");
        String modeValue = directive.getJSONObject("payload").optString("mode","Mode.Normal");
        device = MqttClient.getClient().initClient(modeEndpoint,modeEndpointId );
        int modeInt;
        switch (modeValue){
            case "Mode.Slow":
                modeInt = 2;
                break;
            case "Mode.Fast":
                modeInt = 3;
                break;
            default:
                modeInt = 1;
        }
        State modeState = new State(new Percent("mode",modeInt));
        System.out.println("DATA:"+modeState.toPercentString());
        try {
            device.update(modeState.toPercentString());
        } catch (AWSIotException e) {
            e.printStackTrace();
        }
        ar = new AlexaResponse<Integer>("Alexa", "Response", modeEndpointId, modeToken, correlationToken,false);
        ar.AddContextProperty("Alexa.ModeController","mode",modeValue,500);
        MqttClient.getClient().closeClient();

        return ar;
    }
}
