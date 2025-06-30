package com.devnem0y.tg_bot_assistant_ai.service;

import com.devnem0y.tg_bot_assistant_ai.config.BotConfig;
import com.devnem0y.tg_bot_assistant_ai.config.ModelAi;
import com.github.duzeyyt.or4j.OpenRouter;
import com.github.duzeyyt.or4j.model.Model;
import com.github.duzeyyt.or4j.result.PromptResult;

public class ServiceAi {

    private final String MODEL_OWEN3_235B = "qwen/qwen3-235b-a22b:free";
    private final String MODEL_OWEN3_32B = "qwen/qwen3-32b:free";
    private final String MODEL_GOOGLE = "google/gemma-3-27b-it:free";
    private final String MODEL_DEEPSEEK_R1 = "deepseek/deepseek-r1-0528:free";
    private final String MODEL_DEEPSEEK_V3 = "deepseek/deepseek-chat-v3-0324:free";

    private final OpenRouter openRouter;
    private Model model;

    public ServiceAi(BotConfig config) {
        openRouter = OpenRouter.builder().apiKey(config.getOpenaiKey()).build();
        setModel(ModelAi.qwen3_235b);
    }

    public void setModel(ModelAi modelAi){
        String modelId = switch (modelAi) {
            case qwen3_235b -> MODEL_OWEN3_235B;
            case qwen3_32b -> MODEL_OWEN3_32B;
            case google -> MODEL_GOOGLE;
            case deepseek_r1 -> MODEL_DEEPSEEK_R1;
            case deepseek_v3 -> MODEL_DEEPSEEK_V3;
        };

        model = Model.modelFromId(modelId);
    }

    public String getAnswer(String question){
        try {
            PromptResult result = openRouter.sendPrompt(model, question);
            if (!result.isSuccessful()) return "Не удалось получить ответ, проблема с OpenRouter API Key";
            return result.getResponseMessage();
        } catch (OpenRouter.TooManyRequestsException tooManyRequestsException) {
            return "Слишком много запросов, сбавь обороты";
        } catch (Exception e) { e.printStackTrace(System.err);}

        return "Ошибка ИИ модели";
    }
}