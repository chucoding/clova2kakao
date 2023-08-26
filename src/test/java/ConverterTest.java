import com.google.gson.*;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConverterTest {
    @Test
    public void converterTest() throws Exception {

        /*
        String json = "{\n" +
                "    \"userId\": \"chucoding\",\n" +
                "    \"version\": \"v1\",\n" +
                "    \"timestamp\": 1692677548439,\n" +
                "    \"event\": \"send\",\n" +
                "    \"content\": [\n" +
                "        {\n" +
                "            \"type\": \"text\",\n" +
                "            \"data\": {\n" +
                "                \"details\": \"안녕하세요 저는 AI와 클라우드를 좋아하는 외계공룡 입니다. 만나서 반갑습니다!\"\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";
*/
        // 1. kakao request에서 질문을 받아서 clova로 넘기기

        String userId = "chucoding";
        String details = "자기소개 부탁해";

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

        URL url = new URL("https://em6pb5ys9n.apigw.ntruss.com/custom/v1/11359/33c7aa0f5c1b46f3ac4c469171a534271033589dd880b48de6084374cf06e28d");
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
        JsonObject kakaoPayload = new JsonObject();
        JsonArray outputs = new JsonArray();
        JsonObject output = new JsonObject();
        JsonObject simpleText = new JsonObject();
        simpleText.addProperty("text", resDetails);
        output.add("simpleText", simpleText);
        outputs.add(output);
        template.add("outputs", outputs);
        kakaoPayload.addProperty("version", "2.0");
        kakaoPayload.add("template", template);

        response.add("payload", kakaoPayload);

        System.out.println(response);
/*
        {
            "version": "2.0",
                "template": {
                    "outputs": [
                    {
                        "simpleText": {
                        "text": "급여 지급일은 매월 21일에 지급하며 20일이 공휴일인 경우 그 전일에 지급됩니다.\n더 자세한 사항은 관리부(내선번호 123)로 연락주시기 바랍니다.\n"
                    }
                    }
                ]
            }
        }
*/
    }
}
