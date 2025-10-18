package com.devnem0y.tg_bot_assistant_ai.service;

import com.devnem0y.tg_bot_assistant_ai.config.BotConfig;
import com.devnem0y.tg_bot_assistant_ai.config.ModelAi;
import com.github.duzeyyt.or4j.OpenRouter;
import com.github.duzeyyt.or4j.model.Model;
import com.github.duzeyyt.or4j.result.PromptResult;
import lombok.Getter;

public class ServiceAi {

    private final String MODEL_DEEPSEEK_R1T2_CHIMERA = "tngtech/deepseek-r1t2-chimera:free";
    private final String MODEL_OWEN3_235B = "qwen/qwen3-235b-a22b:free";
    private final String MODEL_GPT_OSS_20B = "openai/gpt-oss-20b:free";
    private final String MODEL_GOOGLE_GEMMA_3_27B = "google/gemma-3-27b-it:free";

    private final OpenRouter openRouter;
    private Model model;

    @Getter
    private String currentModelName;

    @Getter
    private boolean responseReceived;

    public ServiceAi(BotConfig config) {
        openRouter = OpenRouter.builder().apiKey(config.getOpenaiKey()).build();
        setModel(ModelAi.deepseek_r1t2_chimera);
    }

    public void setModel(ModelAi modelAi){
        String modelId = switch (modelAi) {
            case deepseek_r1t2_chimera -> MODEL_DEEPSEEK_R1T2_CHIMERA;
            case qwen3_235b -> MODEL_OWEN3_235B;
            case gpt_oss_20b -> MODEL_GPT_OSS_20B;
            case gemma_3_27b -> MODEL_GOOGLE_GEMMA_3_27B;
        };

        model = Model.modelFromId(modelId);
        currentModelName = modelAi.toString();
    }

    public String getAnswer(String question){
        responseReceived = false;

        try {
            PromptResult result = openRouter.sendPrompt(model, question);
            if (!result.isSuccessful()) {
                responseReceived = true;
                return "Не удалось получить ответ, проблема с OpenRouter API.";
            }

            responseReceived = true;
            return result.getResponseMessage();
        } catch (OpenRouter.TooManyRequestsException tooManyRequestsException) {
            responseReceived = true;
            return "Слишком много запросов, модель " + model.getName() + " перегружена или не отвечает.";
        } catch (Exception e) { e.printStackTrace(System.err);}

        responseReceived = true;
        return "Ошибка модели " + model.getName() + ".";
    }
}