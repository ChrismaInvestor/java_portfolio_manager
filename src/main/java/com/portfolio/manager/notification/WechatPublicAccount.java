package com.portfolio.manager.notification;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service("WechatPublicAccount")
public class WechatPublicAccount implements Notification {
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    public void send(String title, String content) {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String token = "80fbbe7701a142b0808ca94586d4c1c7";
        RequestBody body = RequestBody.create(String.format("title=%s&topic=SAINT&token=%s&content=%s&template=html", title, token, content).getBytes(), mediaType);
        Request request = new Request.Builder()
                .url("http://www.pushplus.plus/send")
                .method("POST", body)
                .build();
        try (Response response = this.httpClient.newCall(request).execute()) {
            log.info("response: {}", response);
        } catch (IOException e) {
            log.error("Notification failed title: {} content:{}", title, content);
        }
    }

    public static void main(String[] args_) throws Exception {
        Notification notification = new WechatPublicAccount();
        notification.send("GOAT notification test", "test");
    }
}
