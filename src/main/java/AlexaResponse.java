// -*- coding: utf-8 -*-

// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

// Licensed under the Amazon Software License (the "License"). You may not use this file except in
// compliance with the License. A copy of the License is located at

//    http://aws.amazon.com/asl/

// or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for the specific
// language governing permissions and limitations under the License.

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import org.json.*;

public class AlexaResponse<T> {

    private JSONObject response = new JSONObject("{}");
    private JSONObject event = new JSONObject("{}");
    private JSONObject header = new JSONObject("{}");
    private JSONObject endpoint = new JSONObject("{}");
    private JSONObject payload = new JSONObject("{}");

    private String CheckValue(String value, String defaultValue) {

        if (value.isEmpty())
            return defaultValue;

        return value;
    }

    public AlexaResponse() {
         this("Alexa", "Response", "INVALID", "INVALID", null,false);
    }

    public AlexaResponse(String namespace, String name, boolean isDiscovery) { this(namespace, name, "INVALID", "INVALID", null, isDiscovery); }

    public AlexaResponse(String namespace, String name, String endpointId, String token, String correlationToken, boolean isDiscovery) {

        header.put("namespace", CheckValue(namespace, "Alexa"));
        header.put("name", CheckValue(name,"Response"));
        header.put("messageId", UUID.randomUUID().toString());
        header.put("payloadVersion", "3");
//        if (payloadData != null){
//            String payload = "{\"change\":{\"cause\":{\"type\":\"PHYSICAL_INTERACTION\"},\"properties\":["+payloadData+"]}}";
//            this.payload = new JSONObject(payload);
//        }

        if (correlationToken != null) {
            header.put("correlationToken", CheckValue(correlationToken, "INVALID"));
        }

        JSONObject scope = new JSONObject("{}");
        scope.put("type", "BearerToken");
        scope.put("token", CheckValue(token, "INVALID"));

        endpoint.put("scope", scope);
        endpoint.put("endpointId", CheckValue(endpointId, "INVALID"));

        event.put("header", header);
        if (!isDiscovery){
            event.put("endpoint", endpoint);
        }
        event.put("payload", payload);

        response.put("event", event);
    }

    public void AddCookie(String key, String value) {
        JSONObject endpointObject = response.getJSONObject("event").getJSONObject("endpoint");
        JSONObject cookie;
        if (endpointObject.has("cookie")) {

            cookie = endpointObject.getJSONObject("cookie");
            cookie.put(key, value);

        } else {
            cookie = new JSONObject();
            cookie.put(key, value);
            endpointObject.put("cookie", cookie);
        }

    }

    public void AddPayloadEndpoint(String friendlyName, String endpointId, String capabilities) {

        JSONObject payload = response.getJSONObject("event").getJSONObject("payload");

        if (payload.has("endpoints"))
        {
            JSONArray endpoints = payload.getJSONArray("endpoints");
            endpoints.put(new JSONObject(CreatePayloadEndpoint(friendlyName, endpointId, capabilities, null)));
        }
        else
        {
            JSONArray endpoints = new JSONArray();
            endpoints.put(new JSONObject(CreatePayloadEndpoint(friendlyName, endpointId, capabilities, null)));
            payload.put("endpoints", endpoints);
        }
    }

    public void AddContextProperty(String namespace, String name, T value, int uncertaintyInMilliseconds)
    {
        if (!namespace.isEmpty()){
            JSONObject context;
            JSONArray properties;
            try {
                context = response.getJSONObject("context");
                properties = context.getJSONArray("properties");

            } catch (JSONException jse) {
                context = new JSONObject();
                properties = new JSONArray();
                context.put("properties", properties);
            }

            properties.put(new JSONObject(CreateContextProperty(namespace, name, value, uncertaintyInMilliseconds)));
            response.put("context", context);
        }else {
            response.put("context", new JSONObject("{}"));
        }
    }

    public String CreateContextProperty(String namespace, String name, T value, int uncertaintyInMilliseconds) {

        JSONObject property = new JSONObject();
        property.put("namespace", namespace);
        property.put("name", name);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
        TimeZone tz = TimeZone.getTimeZone("UTC");
        sdf.setTimeZone(tz);
        String timeOfSample = sdf.format(new Date().getTime());

        property.put("timeOfSample", timeOfSample);
        property.put("uncertaintyInMilliseconds", uncertaintyInMilliseconds);

        // Handle either a JSON Object or value
        property.put("value", value);
//        try {
//            property.put("value", new JSONObject(value));
//        } catch (org.json.JSONException je) {
//            property.put("value", value);
//        }

        return property.toString();
    }

    public String CreatePayloadEndpoint(String friendlyName, String endpointId, String capabilities, String cookie){
        JSONObject endpoint = new JSONObject();
        endpoint.put("capabilities", new JSONArray(capabilities));
        endpoint.put("description", friendlyName + " Endpoint Description");
//        if (endpointId.contains("myiot")){
//            endpoint.put("description", "bedroom room Endpoint Description");
//        }else if (endpointId.contains("kitchen_room")){
//            endpoint.put("description", "Kitchen room Endpoint Description");
//        }else {
//            endpoint.put("description", "corridor Endpoint Description");
//        }

        JSONArray displayCategories = new JSONArray("[\"SPEAKER\"]");
        endpoint.put("displayCategories", displayCategories);
        endpoint.put("manufacturerName", "Sample Manufacturer");

        if (endpointId == null)
            endpointId = "endpoint_" + 100000 + new Random().nextInt(900000);
        endpoint.put("endpointId", endpointId);

        if (friendlyName == null)
            friendlyName = "Sample Endpoint";
        endpoint.put("friendlyName", friendlyName);

        if (cookie != null)
            endpoint.put("cookie", new JSONObject(cookie));

        return endpoint.toString();
    }

    public String CreatePayloadEndpointCapability(String type, String interfaceValue, String version, String properties,
                                                  String instance, String capabilityResources, String configuration, String semantics) {

        JSONObject capability = new JSONObject();
        capability.put("type", type);
        capability.put("interface", interfaceValue);
        capability.put("version", version);
        if (instance != null)
            capability.put("instance",instance);
        if (properties != null)
            capability.put("properties", new JSONObject(properties));
        if (capabilityResources != null)
            capability.put("capabilityResources",new JSONObject(capabilityResources));
        if (configuration != null)
            capability.put("configuration",new JSONObject(configuration));
        if (semantics != null)
            capability.put("semantics",new JSONObject(semantics));
        return capability.toString();
    }

    public void SetPayload(String payload) {
        response.getJSONObject("event").put("payload", new JSONObject(payload));
    }

    @Override
    public String toString() {
        return response.toString();
    }
}
