package com.devnem0y.tg_bot_assistant_ai.telegram;

import com.devnem0y.tg_bot_assistant_ai.config.ModelAi;
import com.devnem0y.tg_bot_assistant_ai.service.ServiceAi;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.time.Duration;
import java.util.List;

public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final ServiceAi serviceAi;

    public UpdateConsumer(TelegramClient client, ServiceAi serviceAi) {
        this.telegramClient = client;
        this.serviceAi = serviceAi;
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
    private void sendMessageTyping(Long chatId) {
        SendChatAction action = new SendChatAction(chatId.toString(), "typing");

        var timeout = Duration.ofSeconds(5); //TODO: Пока хардкодим 5 секунд, нужно будет переделать

        while (!serviceAi.isResponseReceived()) {
            telegramClient.execute(action);
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                System.err.println("Ошибка при задержке: " + e.getMessage());
                break;
            }
        }
    }

    @SneakyThrows
    private void sendMessage(String message, Long chatId) {
        telegramClient.execute(SendMessage.builder().chatId(chatId).text(message).build());
    }

    @SneakyThrows
    private void sendMessageAi(String question, Long chatId) {
        new Thread(() -> sendMessageTyping(chatId)).start();

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
