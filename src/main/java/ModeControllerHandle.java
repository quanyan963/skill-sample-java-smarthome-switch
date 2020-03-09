import bean.Desired;
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
        String instance = directive.getJSONObject("header").optString("instance","");
        //设置endpointId为thingName
        String modeEndpointId = directive.getJSONObject("endpoint").optString("endpointId", "INVALID");
        String modeToken = directive.getJSONObject("endpoint").getJSONObject("scope").optString("token", "INVALID");
        String modeStateValue = directive.getJSONObject("header").optString("name");
        String modeValue = directive.getJSONObject("payload").optString("mode","Mode.Normal");
        device = MqttClient.getClient().initClient(modeEndpoint,modeEndpointId );


        State modeState;
        switch (instance){
            case "Fox.Range":
                modeState = doRange(modeStateValue,modeValue);
                break;
            case "Fox.Mode":
            case "Fox.Timer":
                modeState = doSound_Timer(modeValue);
                break;
            default:
                modeState = null;
                break;
        }

        try {
            if (modeState != null){
                System.out.println("DATA:"+modeState.toPercentString());
                device.update(modeState.toPercentString());
                ar = new AlexaResponse<Integer>("Alexa", "Response", modeEndpointId, modeToken, correlationToken,false);
                ar.AddContextProperty("Alexa.ModeController","mode",modeValue,500);
            }else {
                ar = new AlexaResponse("Alexa","ErrorResponse",modeEndpointId,modeToken,correlationToken,false);
                ar.SetPayload("{\"type\": \"INVALID_DIRECTIVE\",\"message\": \"The directive is not supported by the skill.\"}");
            }
        } catch (AWSIotException e) {
            ar = new AlexaResponse<Integer>("Alexa", "ErrorResponse", modeEndpointId, modeToken, correlationToken,false);
            ar.SetPayload("{\"type\": \"INTERNAL_ERROR\",\"message\": \"A runtime exception occurred. We recommend that you always send a more specific error type.\"}");
        }
        MqttClient.getClient().closeClient();
        return ar;
    }

    private State doSound_Timer(String modeValue) {
        State modeState;
        switch (modeValue){
            case "Sound.Lullaby":
                modeState = new State(new Percent("sound",1));
                return modeState;
            case "Sound.Greensleeves":
                modeState = new State(new Percent("sound",2));
                return modeState;
            case "Sound.Canon":
                modeState = new State(new Percent("sound",3));
                return modeState;
            case "Sound.Waves":
                modeState = new State(new Percent("sound",4));
                return modeState;
            case "Sound.Rain":
                modeState = new State(new Percent("sound",5));
                return modeState;
            case "Sound.Noise":
                modeState = new State(new Percent("sound",6));
                return modeState;
            case "Duration.None":
                modeState = new State(new Percent("duration",0));
                return modeState;
            case "Duration.Fifteen":
                modeState = new State(new Percent("duration",15));
                return modeState;
            case "Duration.Thirty":
                modeState = new State(new Percent("duration",30));
                return modeState;
            case "Duration.Sixty":
                modeState = new State(new Percent("duration",60));
                return modeState;
            default:
                //关闭sound
                modeState = new State(new Percent("sound",0));
                return modeState;
        }
    }

    private State doRange(String modeStateValue, String modeValue) {
        State modeState;
        int value = Integer.parseInt(modeValue);
        switch (modeStateValue){
            case "AdjustRangeValue":
                try {
                    String brightnessResult = device.get();
                    JSONObject data = new JSONObject(brightnessResult);
                    int resultData = data.getJSONObject("state").getJSONObject("desired").optInt("volume",0);
                    switch (resultData){
                        case 0:
                            value = value < 0 ? 0 : value;
                            break;
                        case 10:
                            value = value > 0 ? 10 : 10 - value;
                            break;
                        default:
                            value = resultData + value;
                            if (value < 0){
                                value = 0;
                            }else if (value > 10){
                                value = 10;
                            }
                            break;
                    }
                } catch (AWSIotException e) {
                    e.printStackTrace();
                }
                break;
            default:
                if (value < 0){
                    value = 0;
                }else if (value > 10){
                    value = 10;
                }
                break;
        }
        modeState = new State(new Percent("volume",value));
        return modeState;

    }
}
