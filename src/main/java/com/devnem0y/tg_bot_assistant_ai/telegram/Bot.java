package com.devnem0y.tg_bot_assistant_ai.telegram;

import com.devnem0y.tg_bot_assistant_ai.config.BotConfig;
import com.devnem0y.tg_bot_assistant_ai.config.ModelAi;
import com.devnem0y.tg_bot_assistant_ai.service.ServiceAi;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import java.util.List;

@Component
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {


    private final TelegramClient telegramClient;
    private final BotConfig config;

    private ServiceAi serviceAi;

    public Bot(BotConfig config) {
        this.config = config;
        serviceAi = new ServiceAi(config);
        telegramClient = new OkHttpTelegramClient(config.getToken());
        setCommands();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
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

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var messageText = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();

            if (messageText.charAt(0) == '/') {
                if (messageText.equals("/start")) {
                    sendMessage("\uD83E\uDD16 Привет! Я ваш персональный ИИ помощник в Telegram   \n" +
                            "Готов облегчить ваше обучение, работу и повседневные задачи.  \n" +
                            "\n" +
                            "### Чем могу помочь?  \n" +
                            "\uD83D\uDD0D Многофункциональность:  \n" +
                            "Объясню сложные темы, сформулирую письма, составлю расписание, подберу книги/фильмы, переведу текст, сгенерирую идеи и даже поиграю в квиз.  \n" +
                            "\n" +
                            "\uD83D\uDCAC Естественный диалог:  \n" +
                            "Беседа с вами «как с человеком» — понимаю контекст, запоминаю предпочтения и адаптируюсь под стиль общения.  \n" +
                            "\n" +
                            "\uD83C\uDF0D Многофункциональный помощник:  \n" +
                            "Работаю с текстом, проверяю грамматику, даю советы по тайм-менеджменту, помогаю в учебе, бизнесе и личных проектах.  \n" +
                            "\n" +
                            "### Как начать?  \n" +
                            "Просто задай вопрос и я постараюсь тебе помочь!\n" +
                            "Есть несколько ИИ моделей на выбор.\n" +
                            "\n" +
                            "\uD83C\uDF10 Примеры запросов:  \n" +
                            "- \"Как написать резюме?\"  \n" +
                            "- \"Помоги решить задачу по математике\"  \n" +
                            "- \"Что читать, если любишь фантастику?\"", chatId);
                }
                else if (messageText.equals("/selectmodel")) {
                    sendModelMenu(chatId);
                }
                else if (messageText.equals("/currentmodel")) {
                    sendMessage("Текущая модель " + serviceAi.getCurrentModelName(), chatId);
                }
                else {
                    sendMessage("Такой команды нет", chatId);
                }
            }
            else {
                sendMessage("Ищу ответ, пожалуйста подожди...", chatId);
                new Thread(() -> sendMessageAi(messageText, chatId)).start();
            }
        }
        else if (update.hasCallbackQuery()) {
            var callbackData = update.getCallbackQuery().getData();
            var chatId = update.getCallbackQuery().getMessage().getChatId();
            serviceAi.setModel(ModelAi.valueOf(callbackData));
            sendMessage("Использовать модель " + callbackData, chatId);
        }
    }

    @SneakyThrows
    private void sendMessage(String message, Long chatId) {
        telegramClient.execute(SendMessage.builder().chatId(chatId).text(message).build());
    }

    @SneakyThrows
    private void sendMessageAi(String question, Long chatId) {
        var response = serviceAi.getAnswer(question);
        var maxLength = 4096; //TODO: Возможно, стоит вынести в конфиг

        for (int i = 0; i < response.length(); i += maxLength) {
            String chunk = response.substring(i, Math.min(i + maxLength, response.length()));
            SendMessage message = SendMessage.builder().chatId(chatId).text(chunk).parseMode("Markdown").build();
            telegramClient.execute(message);
        }
    }

    @SneakyThrows
    private void sendModelMenu(Long chatId) {
        SendMessage message = SendMessage.builder().chatId(chatId)
                .text("Какую модель ИИ ты хочешь использовать?\n").build();

        var button1 = InlineKeyboardButton.builder().text("deepseek_v3 (на основе GPT4) ⭐\uFE0F⭐\uFE0F⭐\uFE0F⭐\uFE0F⭐\uFE0F").callbackData("deepseek_v3").build();
        var button2 = InlineKeyboardButton.builder().text("qwen3_235b ⭐\uFE0F⭐\uFE0F⭐\uFE0F⭐\uFE0F⭐\uFE0F").callbackData("qwen3_235b").build();
        var button3 = InlineKeyboardButton.builder().text("deepseek_r1 ⭐\uFE0F⭐\uFE0F⭐\uFE0F⭐\uFE0F").callbackData("deepseek_r1").build();
        var button4 = InlineKeyboardButton.builder().text("qwen3_32b ⭐\uFE0F⭐\uFE0F⭐\uFE0F").callbackData("qwen3_32b").build();
        var button5 = InlineKeyboardButton.builder().text("google_gemma_3_27b ⭐\uFE0F⭐\uFE0F").callbackData("gemma_3_27b").build();
        var button6 = InlineKeyboardButton.builder().text("google_gemini_2.0 ⭐\uFE0F").callbackData("gemini_2").build();
        var button7 = InlineKeyboardButton.builder().text("llama_3.2_11b ⭐\uFE0F").callbackData("llama_3_2_11b").build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3),
                new InlineKeyboardRow(button4),
                new InlineKeyboardRow(button5),
                new InlineKeyboardRow(button6),
                new InlineKeyboardRow(button7)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        message.setReplyMarkup(markup);
        telegramClient.execute(message);
    }
}
