package com.example.demo.config;

import com.linecorp.bot.client.ChannelTokenSupplier;
import com.linecorp.bot.client.FixedChannelTokenSupplier;
import com.linecorp.bot.client.LineMessagingClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LineBotClientConfig {

    @Bean
    @ConditionalOnMissingBean(ChannelTokenSupplier.class)
    @ConditionalOnProperty(prefix = "line.bot", name = "channel-token")
    public ChannelTokenSupplier channelTokenSupplier(
            @Value("${line.bot.channel-token}") String channelToken) {
        return FixedChannelTokenSupplier.of(channelToken);
    }

    @Bean
    @ConditionalOnMissingBean(LineMessagingClient.class)
    @ConditionalOnProperty(prefix = "line.bot", name = "channel-token")
    public LineMessagingClient lineMessagingClient(ChannelTokenSupplier channelTokenSupplier) {
        return LineMessagingClient.builder(channelTokenSupplier).build();
    }
}
