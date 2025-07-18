package com.devnem0y.tg_bot_assistant_ai.telegram;

import com.devnem0y.tg_bot_assistant_ai.config.BotConfig;
import com.devnem0y.tg_bot_assistant_ai.service.ServiceAi;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import java.util.List;

@Component
public class Bot implements SpringLongPollingBot {

    private final TelegramClient telegramClient;
    private final BotConfig config;
    private final UpdateConsumer updateConsumer;

    public Bot(BotConfig config) {
        this.config = config;
        ServiceAi serviceAi = new ServiceAi(config);
        telegramClient = new OkHttpTelegramClient(config.getToken());
        updateConsumer = new UpdateConsumer(telegramClient, serviceAi);
        setCommands();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }

    @SneakyThrows
    public void setCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("/start", "Запуск бота"),
                new BotCommand("/selectmodel", "Выбрать модель ИИ"),
                new BotCommand("/currentmodel", "Текущая модель ИИ")
        );

        telegramClient.execute(new SetMyCommands(commands));
    }
}
