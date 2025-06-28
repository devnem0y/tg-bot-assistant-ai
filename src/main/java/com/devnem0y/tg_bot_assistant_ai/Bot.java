package com.devnem0y.tg_bot_assistant_ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    @Value("${telegram.bot.token}")
    private String token;

    private ServiceAi serviceAi;

    public Bot(@Value("${telegram.bot.token}") String token) {
        telegramClient = new OkHttpTelegramClient(token);
        serviceAi = new ServiceAi();
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            try {
                var message_text = update.getMessage().getText();
                var chat_id = update.getMessage().getChatId();
                var response = serviceAi.getAnswer(message_text);
                var maxLength = 4096;

                for (int i = 0; i < response.length(); i += maxLength) {
                    String chunk = response.substring(i, Math.min(i + maxLength, response.length()));
                    SendMessage message = SendMessage
                            .builder()
                            .chatId(chat_id)
                            .text(chunk)
                            .build();
                    telegramClient.execute(message);
                }
            } catch (TelegramApiException e) { e.printStackTrace(); }
        }
    }
}
