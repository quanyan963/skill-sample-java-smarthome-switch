import bean.Percent;
import bean.State;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import org.json.JSONObject;

public class BrightnessControllerHandle {
    private static BrightnessControllerHandle handle;
    private AlexaResponse ar;
    private AWSIotDevice device;

    public static BrightnessControllerHandle getInstance() {
        if (handle == null){
            handle = new BrightnessControllerHandle();
        }
        return handle;
    }

    public AlexaResponse doReasponse(JSONObject directive,String correlationToken){
        JSONObject brightnessEndpoint = (JSONObject) directive.get("endpoint");
        //设置endpointId为thingName
        String brightnessEndpointId = directive.getJSONObject("endpoint").optString("endpointId", "INVALID");
        String brightnessToken = directive.getJSONObject("endpoint").getJSONObject("scope").optString("token", "INVALID");
        String brightnessStateValue = directive.getJSONObject("header").optString("name");
        device = MqttClient.getClient().initClient(brightnessEndpoint,brightnessEndpointId );
        int brightnessValue;

        if (brightnessStateValue.equals("AdjustBrightness")){
            brightnessValue = directive.getJSONObject("payload").optInt("brightnessDelta",0);
            try {
                brightnessValue = getPercentValue(brightnessValue);
            } catch (AWSIotException e) {
                e.printStackTrace();
            }
        }else {
            brightnessValue = directive.getJSONObject("payload").optInt("brightness",0);
        }

        State brightnessState = new State(new Percent("percent",brightnessValue));
        System.out.println("DATA:"+brightnessState.toPercentString());

        //更新
        try {
            device.update(brightnessState.toPercentString());
        } catch (AWSIotException e) {
            e.printStackTrace();
        }
        ar = new AlexaResponse<Integer>("Alexa", "Response", brightnessEndpointId, brightnessToken, correlationToken,false);
        ar.AddContextProperty("Alexa.BrightnessController","brightness",brightnessValue,500);
        MqttClient.getClient().closeClient();
        return ar;
    }

    private int getPercentValue(int percentValue) throws AWSIotException {
        String brightnessResult = device.get();
        JSONObject data = new JSONObject(brightnessResult);
        int resultData = data.getJSONObject("state").getJSONObject("desired").optInt("percent",0);
        int point = resultData / 25;
        switch (point){
            case 0:
                if (percentValue > 0){
                    return 25;
                }else {
                    return 0;
                }
            case 1:
                if (percentValue > 0){
                    return 50;
                }else {
                    return 25;
                }
            case 2:
                if (percentValue > 0){
                    return 75;
                }else {
                    return 50;
                }
            case 3:
            case 4:
                if (percentValue > 0){
                    return 100;
                }else {
                    return 75;
                }
            default:
                return 0;
        }

    }
}
