import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.util.IOUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DiscoveryHandle {
    private static DiscoveryHandle handle;
    private static HashMap<String, JSONObject> pems = new HashMap<>();
    private static AlexaResponse ar;
    private static String userId;

    public static DiscoveryHandle getInstance() {
        if (handle == null) {
            handle = new DiscoveryHandle();
        }
        return handle;
    }

//    public static void main(String args[]) {
//        doResponse("eyJraWQiOiJNdjB6aUhGc2dUZCtMZHR6ZEU2S0pzQmxNU1gwR2djRVUybzdUR3NKNVwvOD0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJlYWRjMjEzZi0wZDQ3LTQyNjktOWY0Ny1jYjBiZmViZGEzYWIiLCJjb2duaXRvOmdyb3VwcyI6WyJ1cy1lYXN0LTFfYUw4N2VaM1pZX0xvZ2luV2l0aEFtYXpvbiJdLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIiwiYXV0aF90aW1lIjoxNTgzMzg4NDQ1LCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAudXMtZWFzdC0xLmFtYXpvbmF3cy5jb21cL3VzLWVhc3QtMV9hTDg3ZVozWlkiLCJleHAiOjE1ODMzOTIwNDUsImlhdCI6MTU4MzM4ODQ0NSwidmVyc2lvbiI6MiwianRpIjoiZDNiY2FkNDMtOGFhNi00ZGM5LTkxMmUtNjIwYzczZjhhNGJlIiwiY2xpZW50X2lkIjoiN3NrZTQwMmxjMGc3a202Ymdmdjh1bmtlMW8iLCJ1c2VybmFtZSI6IkxvZ2luV2l0aEFtYXpvbl9hbXpuMS5hY2NvdW50LkFITUpVSEtRUVRJTE1KTzZGNFlTRlFCUlVEVUEifQ.HX30OWzKQsOXJ9DWOXxXu6-SbrtZtsVOorwtYM0t_sfja4QVoa1UT9K_0idiyIH3xi3xZGUo3-qci3IGZTPtIoYSS4i11CYO-sQPZYROi-RKEZsrIreqp9RZ4O7AeTZMaNTqGaFlb9JGM41N3pB9PDWdBRDrVHT2GkXs7Gmi4GvwGMZaU9jVghLHp8M9mQxkl8rnHnseFxXYkRjS82RE0hOho3RlfDPCsJTuW5a0yhLxLWuizfKhyfRsUQfgnBnglYyAO_OQiB-Q9FMWyYTtxgsxc8qXTGwyO9yrS2N8Gp1oOdgmGcrm9hKh0lUWOw1wqueOLW51HdBuaJ1SRd9Drg");
//    }

    public AlexaResponse doResponse(JSONObject directive,String correlationToken) {//
        String token = directive.getJSONObject("payload").getJSONObject("scope").optString("token");//

        try {
            //请求公钥
            URL url = new URL("https://cognito-idp.us-east-1.amazonaws.com:443/" + Config.PoolId + "/.well-known/jwks.json");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("GET");
            //获取数据
            InputStream response = connection.getInputStream();
            String responseString = IOUtils.toString(response);
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray keys = jsonObject.getJSONArray("keys");
            for (int i = 0; i < keys.length(); i++) {
                //Convert each key to PEM
                JSONObject data = new JSONObject(keys.get(i).toString());
                String key_id = data.optString("kid");
//                String modulus = data.optString("n");
//                String exponent = data.optString("e");
//                String key_type = data.optString("kty");
                //jwk to pem
                pems.put(key_id, data);//JwkToPem.getInstance().main(key_type,modulus,exponent)
            }
            try {
                DecodedJWT jwt = JWT.decode(token);
                String kid = jwt.getKeyId();
                JSONObject pem = pems.get(kid);
                //解码并生成RSAPublicKey
                byte[] modulusBytes = Base64.getUrlDecoder().decode(pem.optString("n"));
                BigInteger modulusInt = new BigInteger(1, modulusBytes);
                byte[] exponentBytes = Base64.getUrlDecoder().decode(pem.optString("e"));
                BigInteger exponentInt = new BigInteger(1, exponentBytes);
                KeyFactory keyFactory = KeyFactory.getInstance(pem.optString("kty"));
                RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(modulusInt, exponentInt);
                RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(publicSpec);

                Algorithm algorithm = Algorithm.RSA256(pubKey);
                JWTVerifier verifier = JWT.require(algorithm)
                        .withIssuer("https://cognito-idp.us-east-1.amazonaws.com/" + Config.PoolId)
                        .build(); //Reusable verifier instance
                jwt = verifier.verify(token);
                //获取用户名
                userId = jwt.getClaims().get("username").asString();

                String[] values = userId.split("\\.");
                Map<String, AttributeValue> deviceData = sendDeviceState(values[values.length - 1]);
                if (deviceData != null) {
                    ar = new AlexaResponse("Alexa.Discovery", "Discover.Response", true);
                    String[] friendlyNames = deviceData.keySet().toArray(new String[deviceData.size()]);
                    for (int i = 0; i < friendlyNames.length; i++) {
                        String thingName = deviceData.get(friendlyNames[i]).getS();

                        //create device
                        String bedRoomCapabilities = createCapabilities(ar, friendlyNames[i]);
                        ar.AddPayloadEndpoint(friendlyNames[i], thingName, bedRoomCapabilities);

                    }
                    return ar;
                } else {
                    ar = new AlexaResponse("Alexa","ErrorResponse","","",correlationToken,false);
                    ar.SetPayload("{\"type\": \"NO_SUCH_ENDPOINT\",\"message\": \"No device was found.Please add device on App first.\"}");
                    return ar;
                }
            } catch (Exception ex) {
                ar = new AlexaResponse("Alexa","ErrorResponse","","",correlationToken,false);
                ex.printStackTrace();
                ar.SetPayload("{\"type\": \"INTERNAL_ERROR\",\"message\": \"A runtime exception occurred. We recommend that you always send a more specific error type.\"}");
                return ar;
            }


        } catch (Exception e) {
            ar = new AlexaResponse("Alexa","ErrorResponse","","",correlationToken,false);
            e.printStackTrace();
            ar.SetPayload("{\"type\": \"INTERNAL_ERROR\",\"message\": \"A runtime exception occurred. We recommend that you always send a more specific error type.\"}");
            return ar;
        }

//        ar = new AlexaResponse("Alexa.Discovery", "Discover.Response", true);
//
//        String bedRoomCapabilities = createCapabilities(ar, "paper");
//        ar.AddPayloadEndpoint("paper", "asdffgqe", bedRoomCapabilities);
//        return ar;
    }

    private Map<String, AttributeValue> sendDeviceState(String value) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withCredentials(new MyCredentialsProvider()).build();

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("UserId", new AttributeValue().withS(value));
        GetItemResult result = client.getItem(new GetItemRequest().withTableName("FOX").withKey(key));
        if (result.getItem() != null) {
            //更改数据
            Map<String, AttributeValue> resultItem = result.getItem();
            return resultItem.get("ThingDir").getM();
        } else {
            return null;
        }
    }

    private String createCapabilities(AlexaResponse ar, String friendlyName) {
        String device = ar.CreatePayloadEndpointCapability("AlexaInterface", "Alexa"
                , "3", null, null, null, null,null);
        String alexaPowerController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.PowerController", "3", "{\"supported\": [ { \"name\": \"powerState\" } ] }"
                , null, null, null,null);

//        String bedRoomAlexaPercentageController = ar.CreatePayloadEndpointCapability("AlexaInterface"
//                , "Alexa.PercentageController", "3"
//                , "{\"supported\": [ { \"name\": \"percentage\" } ] ,\"proactivelyReported\":"+true+",\"retrievable\":"+true+"}"
//                ,null, null, null);

//        String bedroomAlexaBrightnessController = ar.CreatePayloadEndpointCapability("AlexaInterface"
//                , "Alexa.BrightnessController", "3"
//                , "{\"supported\": [ { \"name\": \"brightness\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + "}"
//                , null, null, null);

        //音效模式
        String alexaModeController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.ModeController", "3"
                , "{\"supported\": [ { \"name\": \"mode\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + ",\"nonControllable\":" + false + "}"
                , "Fox.Mode", createCapabilityResources("sound"), createModeConfiguration(),null);
        //持续时间
        String alexaTimerController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.ModeController", "3"
                , "{\"supported\": [ { \"name\": \"mode\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + ",\"nonControllable\":" + false + "}"
                , "Fox.Timer", createCapabilityResources("duration"), createDurationConfiguration(),null);
//        //声音大小
//        String alexaVolumeController = ar.CreatePayloadEndpointCapability("AlexaInterface"
//                , "Alexa.ModeController", "3"
//                , "{\"supported\": [ { \"name\": \"mode\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + ",\"nonControllable\":" + false + "}"
//                , "Fox.Volume", createCapabilityResources("sound volume"), createVolumeConfiguration());
        //声音大小
        String alexaRangeController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.RangeController", "3"
                , "{\"supported\": [ { \"name\": \"mode\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + ",\"nonControllable\":" + false + "}"
                , "Fox.Range", createCapabilityResources("sound volume"), createRangeConfiguration(),null);

        //彩灯开关
        String alexaLightsController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.ToggleController", "3"
                , "{\"supported\": [ { \"name\": \"mode\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + ",\"nonControllable\":" + false + "}"
                , "Fox.Lights", createCapabilityResources("lights"), null, createLightsSemanticsResources());

        //彩灯暂停
        String alexaHoldController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.ModeController", "3"//TimeHoldController
                , "{\"supported\": [ { \"name\": \"mode\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + "}"// + ",\"nonControllable\":" + false
                , "Fox.Hold", createCapabilityResources("lights"), createHoldConfiguration(),null);//createCapabilityResources("lights")   "Fox.Hold"
        return "[" + device + ", " + alexaPowerController + "," +
                alexaModeController + "," + alexaTimerController + "," + alexaRangeController + ","
                + alexaLightsController + "," + alexaHoldController + "]";//+ alexaLightsController + ","
    }

    private String createSemanticsResources() {
        return "{" +
                "                \"actionMappings\": [" +
                "                  {" +
                "                    \"@type\": \"ActionsToDirective\"," +
                "                    \"actions\": [\"Alexa.Actions.Close\"]," +
                "                    \"directive\": {" +
                "                      \"name\": \"SetMode\"," +
                "                      \"payload\": {" +
                "                           \"mode\": \"Hold.Close\"}" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"@type\": \"ActionsToDirective\"," +
                "                    \"actions\": [\"Alexa.Actions.Open\"]," +
                "                    \"directive\": {" +
                "                      \"name\": \"SetMode\"," +
                "                      \"payload\": {" +
                "                           \"mode\": \"Hold.Open\"}" +
                "                    }" +
                "                  }" +
                "                ]," +
                "                \"stateMappings\": [" +
                "                  {" +
                "                    \"@type\": \"StatesToValue\"," +
                "                    \"states\": [\"Alexa.States.Closed\"]," +
                "                    \"value\": \"Hold.Close\"" +
                "                  }," +
                "                  {" +
                "                    \"@type\": \"StatesToValue\"," +
                "                    \"states\": [\"Alexa.States.Open\"]," +
                "                    \"value\": \"Hold.Open\"" +
                "                  }  " +
                "                ]" +
                "              }";
    }

    private String createLightsSemanticsResources() {
        return "{" +
                "                \"actionMappings\": [" +
                "                  {" +
                "                    \"@type\": \"ActionsToDirective\"," +
                "                    \"actions\": [\"Alexa.Actions.Close\"]," +
                "                    \"directive\": {" +
                "                      \"name\": \"TurnOff\"," +
                "                      \"payload\": {}" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"@type\": \"ActionsToDirective\"," +
                "                    \"actions\": [\"Alexa.Actions.Open\"]," +
                "                    \"directive\": {" +
                "                      \"name\": \"TurnOn\"," +
                "                      \"payload\": {}" +
                "                    }" +
                "                  }" +
                "               ]," +
                "                \"stateMappings\": [" +
                "                  {" +
                "                    \"@type\": \"StatesToValue\"," +
                "                    \"states\": [\"Alexa.States.Closed\"]," +
                "                    \"value\": \"OFF\"" +
                "                  }," +
                "                  {" +
                "                    \"@type\": \"StatesToValue\"," +
                "                    \"states\": [\"Alexa.States.Open\"]," +
                "                    \"value\": \"ON\"" +
                "                  }  " +
                "                ]" +
                "              }";
    }

    private String createCapabilityResources(String value) {
        return "{" +
                "                \"friendlyNames\": [" +
                "                  {" +
                "                    \"@type\": \"text\"," +
                "                    \"value\": {" +
                "                      \"text\": \""+value+"\"," +
                "                      \"locale\": \"en-US\"" +
                "                    }" +
                "                  }" +
                "                ]" +
                "              }";
    }

    private String createHoldConfiguration() {
        return  "{" +
                "                \"ordered\": false," +
                "                \"supportedModes\": [" +
                "                  {" +
                "                    \"value\": \"Hold.Close\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"asset\"," +
                "                          \"value\": {" +
                "                            \"assetId\": \"Alexa.Value.Close\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Hold.Open\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"asset\"," +
                "                          \"value\": {" +
                "                            \"assetId\": \"Alexa.Value.Open\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Hold.Pause\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"Pause\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }" +
                "                ]" +
                "              }";
//        return "{\n" +
//                "                  \"allowRemoteResume\": true\n" +
//                "              }";
    }

    private String createRangeConfiguration(){
        return "{" +
                "                \"supportedRange\": {" +
                "                  \"minimumValue\": 0," +
                "                  \"maximumValue\": 10," +
                "                  \"precision\": 1" +
                "                }," +
                "                \"presets\": [" +
                "                  {" +
                "                    \"rangeValue\": 10," +
                "                    \"presetResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"asset\"," +
                "                          \"value\": {" +
                "                            \"assetId\": \"Alexa.Value.Maximum\"" +
                "                          }" +
                "                        }," +
                "                        {" +
                "                          \"@type\": \"asset\"," +
                "                          \"value\": {" +
                "                            \"assetId\": \"Alexa.Value.High\"" +
                "                          }" +
                "                        }," +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"highest\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"rangeValue\": 1," +
                "                    \"presetResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"asset\"," +
                "                          \"value\": {" +
                "                            \"assetId\": \"Alexa.Value.Minimum\"" +
                "                          }" +
                "                        }," +
                "                        {" +
                "                          \"@type\": \"asset\"," +
                "                          \"value\": {" +
                "                            \"assetId\": \"Alexa.Value.Low\"" +
                "                          }" +
                "                        }," +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"lowest\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"rangeValue\": 0," +
                "                    \"presetResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"close\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }," +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"silence\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }," +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"mute\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }," +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"off\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }," +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"turn off\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }" +
                "                ]" +
                "              }" +
                "            }";
    }

    private String createModeConfiguration() {
        return "{" +
                "                \"ordered\": false," +
                "                \"supportedModes\": [" +
                "                  {" +
                "                    \"value\": \"Sound.Lullaby\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"lullaby\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Sound.Greensleeves\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"greensleeves\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Sound.Canon\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"canon\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Sound.Waves\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"waves\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Sound.Rain\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"rain\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Sound.Noise\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"white noise\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Sound.Off\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"off\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }," +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"turn off\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }," +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"close\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }" +
                "                ]" +
                "              }";
    }

    private String createDurationConfiguration() {
        return "{" +
                "                \"ordered\": false," +
                "                \"supportedModes\": [" +
                "                  {" +
                "                    \"value\": \"Duration.None\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"none\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Duration.Fifteen\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"fifteen minutes\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Duration.Thirty\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"thirty minutes\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Duration.Sixty\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"sixty minutes\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }" +
                "                ]" +
                "              }";
    }
}
