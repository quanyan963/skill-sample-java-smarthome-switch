import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import org.json.JSONObject;

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

    public AlexaResponse doResponse(JSONObject directive) {//
        String token = directive.getJSONObject("payload").getJSONObject("scope").optString("token");//

//        try {
//            //请求公钥
//            URL url = new URL("https://cognito-idp.us-east-1.amazonaws.com:443/" + Config.PoolId + "/.well-known/jwks.json");
//            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestMethod("GET");
//            //获取数据
//            InputStream response = connection.getInputStream();
//            String responseString = IOUtils.toString(response);
//            JSONObject jsonObject = new JSONObject(responseString);
//            JSONArray keys = jsonObject.getJSONArray("keys");
//            for (int i = 0; i < keys.length(); i++) {
//                //Convert each key to PEM
//                JSONObject data = new JSONObject(keys.get(i).toString());
//                String key_id = data.optString("kid");
////                String modulus = data.optString("n");
////                String exponent = data.optString("e");
////                String key_type = data.optString("kty");
//                //jwk to pem
//                pems.put(key_id, data);//JwkToPem.getInstance().main(key_type,modulus,exponent)
//            }
//            try {
//                DecodedJWT jwt = JWT.decode(token);
//                String kid = jwt.getKeyId();
//                JSONObject pem = pems.get(kid);
//                //解码并生成RSAPublicKey
//                byte[] modulusBytes = Base64.getUrlDecoder().decode(pem.optString("n"));
//                BigInteger modulusInt = new BigInteger(1, modulusBytes);
//                byte[] exponentBytes = Base64.getUrlDecoder().decode(pem.optString("e"));
//                BigInteger exponentInt = new BigInteger(1, exponentBytes);
//                KeyFactory keyFactory = KeyFactory.getInstance(pem.optString("kty"));
//                RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(modulusInt, exponentInt);
//                RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(publicSpec);
//
//                Algorithm algorithm = Algorithm.RSA256(pubKey);
//                JWTVerifier verifier = JWT.require(algorithm)
//                        .withIssuer("https://cognito-idp.us-east-1.amazonaws.com/" + Config.PoolId)
//                        .build(); //Reusable verifier instance
//                jwt = verifier.verify(token);
//                //获取用户名
//                userId = jwt.getClaims().get("username").asString();
//            } catch (Exception ex) {
//                ar = new AlexaResponse();
//            }
//            String[] values = userId.split("\\.");
//            Map<String, AttributeValue> deviceData = sendDeviceState(values[values.length - 1]);
//            if (deviceData != null) {
//                ar = new AlexaResponse("Alexa.Discovery", "Discover.Response", true);
//                String[] friendlyNames = deviceData.keySet().toArray(new String[deviceData.size()]);
//                for (int i = 0; i < friendlyNames.length; i++) {
//                    String thingName = deviceData.get(friendlyNames[i]).getS();
//
//                    //create device
//                    String bedRoomCapabilities = createCapabilities(ar, friendlyNames[i]);
//                    ar.AddPayloadEndpoint(friendlyNames[i], thingName, bedRoomCapabilities);
//
//                }
//                return ar;
//            } else {
//                ar = new AlexaResponse();
//                return ar;
//            }
//
//        } catch (Exception e) {
//            ar = new AlexaResponse();
//            return ar;
//        }

        ar = new AlexaResponse("Alexa.Discovery", "Discover.Response", true);

        String bedRoomCapabilities = createCapabilities(ar, "paper");
        ar.AddPayloadEndpoint("paper", "asdffgqe", bedRoomCapabilities);
        return ar;
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
        String bed_room = ar.CreatePayloadEndpointCapability("AlexaInterface", "Alexa"
                , "3", null, null, null, null);
        String bedRoomAlexaPowerController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.PowerController", "3", "{\"supported\": [ { \"name\": \"powerState\" } ] }"
                , null, null, null);

//        String bedRoomAlexaPercentageController = ar.CreatePayloadEndpointCapability("AlexaInterface"
//                , "Alexa.PercentageController", "3"
//                , "{\"supported\": [ { \"name\": \"percentage\" } ] ,\"proactivelyReported\":"+true+",\"retrievable\":"+true+"}"
//                ,null, null, null);

        String bedroomAlexaBrightnessController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.BrightnessController", "3"
                , "{\"supported\": [ { \"name\": \"brightness\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + "}"
                , null, null, null);

        //音效模式
        String bedroomAlexaModeController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.ModeController", "3"
                , "{\"supported\": [ { \"name\": \"mode\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + ",\"nonControllable\":" + false + "}"
                , "Fox.Mode", createCapabilityResources("sound"), createModeConfiguration());
        //持续时间
        String bedroomAlexaTimerController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.ModeController", "3"
                , "{\"supported\": [ { \"name\": \"mode\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + ",\"nonControllable\":" + false + "}"
                , "Fox.Timer", createCapabilityResources("duration"), createDurationConfiguration());
        //声音大小
        String bedroomAlexaVolumeController = ar.CreatePayloadEndpointCapability("AlexaInterface"
                , "Alexa.ModeController", "3"
                , "{\"supported\": [ { \"name\": \"mode\" } ] ,\"proactivelyReported\":" + true + ",\"retrievable\":" + true + ",\"nonControllable\":" + false + "}"
                , "Fox.Volume", createCapabilityResources("cat"), createVolumeConfiguration());
        return "[" + bed_room + ", " + bedRoomAlexaPowerController + "," + bedroomAlexaBrightnessController + "," +
                bedroomAlexaModeController + "," + bedroomAlexaTimerController + "," + bedroomAlexaVolumeController + "]";//
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

    private String createModeConfiguration() {
        return "{" +
                "                \"ordered\": true," +
                "                \"supportedModes\": [" +
                "                  {" +
                "                    \"value\": \"Sound.Slow\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"slow\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Sound.Normal\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"normal\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Sound.Fast\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"fast\"," +
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

    private String createVolumeConfiguration() {
        return "{" +
                "                \"ordered\": false," +
                "                \"supportedModes\": [" +
                "                  {" +
                "                    \"value\": \"Volume.Silence\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
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
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.One\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"level one\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.Two\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"level two\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.three\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"level three\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }，" +
                "                  {" +
                "                    \"value\": \"Volume.four\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"level four\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.five\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"level five\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.six\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"level six\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.seven\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"level seven\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.eight\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"level eight\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.nine\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"level nine\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.ten\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"level ten\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.up\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"up\"," +
                "                            \"locale\": \"en-US\"" +
                "                          }" +
                "                        }" +
                "                      ]" +
                "                    }" +
                "                  }," +
                "                  {" +
                "                    \"value\": \"Volume.down\"," +
                "                    \"modeResources\": {" +
                "                      \"friendlyNames\": [" +
                "                        {" +
                "                          \"@type\": \"text\"," +
                "                          \"value\": {" +
                "                            \"text\": \"down\"," +
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
