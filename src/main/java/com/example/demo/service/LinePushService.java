package com.example.demo.service;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class LinePushService {

    private static final Logger log = LoggerFactory.getLogger(LinePushService.class);

    private final LineMessagingClient client;

    public LinePushService(ObjectProvider<LineMessagingClient> clientProvider) {
        this.client = clientProvider.getIfAvailable();
    }

    public void pushToUser(String userId, String message) {
        if (client == null) {
            log.warn("LINE push skipped because LineMessagingClient is not configured");
            return;
        }

        TextMessage textMessage = new TextMessage(message);
        PushMessage pushMessage = new PushMessage(userId, textMessage);

        client.pushMessage(pushMessage);
    }
}
