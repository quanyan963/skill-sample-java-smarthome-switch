// -*- coding: utf-8 -*-

// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

// Licensed under the Amazon Software License (the "License"). You may not use this file except in
// compliance with the License. A copy of the License is located at

//    http://aws.amazon.com/asl/

// or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for the specific
// language governing permissions and limitations under the License.

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;

import com.amazonaws.services.lambda.runtime.Context;
import org.json.*;

public class AlexaHandler {
    public  static AlexaResponse ar = new AlexaResponse();
    public static boolean isFinish = false;

//    public static void main(String args[]){
//
//
//    }

    public static void handler(InputStream inputStream, OutputStream outputStream, Context context) {

        String request;
        try {
            request = getRequest(inputStream);
            System.out.println("Request:");
            System.out.println(request);

            JSONObject jsonRequest = new JSONObject(request);
            JSONObject directive = (JSONObject) jsonRequest.get("directive");
            JSONObject header = (JSONObject) directive.get("header");

            String namespace = header.optString("namespace", "INVALID");
            String correlationToken = header.optString("correlationToken", "INVALID");
            switch(namespace) {

                case "Alexa.Authorization":
                    System.out.println("Found Alexa.Authorization Namespace");
                    ar = new AlexaResponse("Alexa.Authorization","AcceptGrant", "INVALID", "INVALID", correlationToken,true);
                    break;

                case "Alexa.Discovery":
                    ar = DiscoveryHandle.getInstance().doResponse(directive);
                    // For another way to see how to craft an AlexaResponse, have a look at AlexaResponseTest:ResponseDiscovery
                    break;
                case "Alexa.PowerController":
                    ar = PowerControllerHandle.getInstance().doResponse(directive,correlationToken);
                    break;
                case "Alexa.BrightnessController":
                    ar = BrightnessControllerHandle.getInstance().doReasponse(directive,correlationToken);
                    break;
                case "Alexa.ModeController":
                    ar = ModeControllerHandle.getInstance().doReasponse(directive,correlationToken);
                    break;
                default:
                    System.out.println("INVALID Namespace");
                    ar = new AlexaResponse();
                    break;
            }
            System.out.println("Response:");
            System.out.println(ar);
            outputStream.write(ar.toString().getBytes(Charset.forName("UTF-8")));


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static String getRequest(java.io.InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
