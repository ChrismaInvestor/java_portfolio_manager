package com.portfolio.manager.moonshot;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MoonshotAiUtils {

    private static final String API_KEY = "sk-a5P9VLre5bODtFT23a3sTShZPAFDJQNw1iDSrVGAHcJ8Jw99";
    private static final String MODELS_URL = "https://api.moonshot.cn/v1/models";
    private static final String FILES_URL = "https://api.moonshot.cn/v1/files";
    private static final String ESTIMATE_TOKEN_COUNT_URL = "https://api.moonshot.cn/v1/tokenizers/estimate-token-count";
    private static final String CHAT_COMPLETION_URL = "https://api.moonshot.cn/v1/chat/completions";

    MoonshotAiUtils(){

    }

    public static ResponseEntity getModelList() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String resStr = getCommonRequest(MODELS_URL)
                .execute()
                .body();
        return mapper.readValue(resStr, ResponseEntity.class);
    }

    public static String uploadFile(@NonNull File file) {
        return getCommonRequest(FILES_URL)
                .method(Method.POST)
                .header("purpose", "file-extract")
                .form("file", file)
                .execute()
                .body();
    }

    public static String getFileList() {
        return getCommonRequest(FILES_URL)
                .execute()
                .body();
    }

    public static String deleteFile(@NonNull String fileId) {
        return getCommonRequest(FILES_URL + "/" + fileId)
                .method(Method.DELETE)
                .execute()
                .body();
    }

    public static String getFileDetail(@NonNull String fileId) {
        return getCommonRequest(FILES_URL + "/" + fileId)
                .execute()
                .body();
    }

    public static String getFileContent(@NonNull String fileId) {
        return getCommonRequest(FILES_URL + "/" + fileId + "/content")
                .execute()
                .body();
    }

    public static String estimateTokenCount(@NonNull String model, @NonNull List<Message> messages) {
        String requestBody = new JSONObject()
                .putOpt("model", model)
                .putOpt("messages", messages)
                .toString();
        return getCommonRequest(ESTIMATE_TOKEN_COUNT_URL)
                .method(Method.POST)
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .body(requestBody)
                .execute()
                .body();
    }

    @SneakyThrows
    public static String chat(@NonNull String model, @NonNull List<Message> messages) {
        String requestBody = new JSONObject()
                .putOpt("model", model)
                .putOpt("messages", messages)
                .putOpt("stream", true)
                .toString();
        Request okhttpRequest = new Request.Builder()
                .url(CHAT_COMPLETION_URL)
                .post(RequestBody.create(requestBody, MediaType.get(ContentType.JSON.getValue())))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();
        Call call = new OkHttpClient().newCall(okhttpRequest);
        Response okhttpResponse = call.execute();
        BufferedReader reader = new BufferedReader(okhttpResponse.body().charStream());
        String line;
        StringBuilder sb = new StringBuilder();log.info("start time");
        while ((line = reader.readLine()) != null) {
            if (StrUtil.isBlank(line)) {
                continue;
            }
            if (JSONUtil.isTypeJSON(line)) {
                Optional.of(JSONUtil.parseObj(line))
                        .map(x -> x.getJSONObject("error"))
                        .map(x -> x.getStr("message"))
                        .ifPresent(x -> System.out.println("error: " + x));
                return null;
            }
            line = StrUtil.replace(line, "data: ", StrUtil.EMPTY);
            if (StrUtil.equals("[DONE]", line) || !JSONUtil.isTypeJSON(line)) {
                log.info("end time");
                return sb.toString();
            }

            Optional.of(JSONUtil.parseObj(line))
                    .map(x -> x.getJSONArray("choices"))
                    .filter(CollUtil::isNotEmpty)
                    .map(x -> (JSONObject) x.get(0))
                    .map(x -> x.getJSONObject("delta"))
                    .map(x -> x.getStr("content"))
                    .ifPresent(x-> {

                        sb.append(x);
                    });
        }
        return sb.toString();
    }

    private static HttpRequest getCommonRequest(@NonNull String url) {
        return HttpRequest.of(url).header(Header.AUTHORIZATION, "Bearer " + API_KEY);
    }

    public static void main(String... args) throws JsonProcessingException {
        MoonshotAiUtils.getModelList().data().forEach(data -> log.info("model: {}", data.id()));
        Message msg = new Message();
        List<Message> msgList = new ArrayList<>();
        msg.setRole(RoleEnum.user);
        msg.setContent("Hi, how are you?");
        msgList.add(msg);
        log.info("startTime");
        var res = MoonshotAiUtils.chat("moonshot-v1-8k", msgList);
        log.info("res: {}", res);
        Message msg2 = new Message();
        msg2.setRole(RoleEnum.user);
        msg2.setContent("Glad to meet you");
        msgList.add(msg2);
        res = MoonshotAiUtils.chat("moonshot-v1-8k", msgList);
        log.info("res: {}", res);
    }

}
