package com.chucoding;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    public static JsonObject main(JsonObject args) throws Exception {
        // 1. kakao request에서 질문을 받아서 clova로 넘기기
        String apiUrl = args.getAsJsonPrimitive("invokeURL").getAsString();

        JsonObject jsonObject = args.getAsJsonObject();
        JsonObject userRequestObject = jsonObject.getAsJsonObject("userRequest");

        String userId = userRequestObject.getAsJsonObject("user").getAsJsonObject("properties").getAsJsonPrimitive("botUserKey").getAsString();
        String details = userRequestObject.getAsJsonPrimitive("utterance").getAsString();

        JsonObject clovaPayload = new JsonObject();
        clovaPayload.addProperty("userId", userId);

        JsonArray content = new JsonArray();
        JsonObject contentBlock = new JsonObject();
        contentBlock.addProperty("type", "text");

        JsonObject data = new JsonObject();
        data.addProperty("details", details);
        contentBlock.add("data", data);
        content.add(contentBlock);

        clovaPayload.add("content", content);
        clovaPayload.addProperty("event", "send");

        String json = new Gson().toJson(clovaPayload);

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(json.getBytes());
        }

        JsonObject response = new JsonObject();

        int responseCode = connection.getResponseCode();
        String resultString = "";
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder result = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();
            resultString = result.toString();
        }

        JsonParser parser = new JsonParser();
        JsonElement rootElement = parser.parse(resultString);
        JsonObject resJsonObject = rootElement.getAsJsonObject();

        JsonObject contentObject = resJsonObject.getAsJsonArray("content").get(0).getAsJsonObject();
        JsonObject resDataObject = contentObject.getAsJsonObject("data");

        String resDetails = resDataObject.get("details").getAsString();

        JsonObject template = new JsonObject();
        JsonArray outputs = new JsonArray();
        JsonObject output = new JsonObject();
        JsonObject simpleText = new JsonObject();
        simpleText.addProperty("text", resDetails);
        output.add("simpleText", simpleText);
        outputs.add(output);
        template.add("outputs", outputs);
        response.addProperty("version", "2.0");
        response.add("template", template);

        return response;
    }
}