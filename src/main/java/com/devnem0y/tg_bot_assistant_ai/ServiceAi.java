package com.devnem0y.tg_bot_assistant_ai;

import com.github.duzeyyt.or4j.OpenRouter;
import com.github.duzeyyt.or4j.model.Model;
import com.github.duzeyyt.or4j.result.PromptResult;

public class ServiceAi {

    private final String API_KEY = "***";
    private final String MODEL_ID = "qwen/qwen3-235b-a22b:free";

    private final OpenRouter openRouter;
    private final Model model;

    public ServiceAi(){
        openRouter = OpenRouter.builder().apiKey(API_KEY).build();
        model = Model.modelFromId(MODEL_ID);
    }

    public String getAnswer(String question){
        try {
            PromptResult result = openRouter.sendPrompt(model, question);
            if (!result.isSuccessful()) return "Failed to get response";
            return result.getResponseMessage();
        } catch (OpenRouter.TooManyRequestsException tooManyRequestsException) {
            return "Too many requests";
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return "";
    }
}
