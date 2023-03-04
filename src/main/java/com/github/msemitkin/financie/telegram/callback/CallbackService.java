package com.github.msemitkin.financie.telegram.callback;

import com.github.msemitkin.financie.telegram.callback.persistence.CallbackEntity;
import com.github.msemitkin.financie.telegram.callback.persistence.CallbackRepository;
import com.google.gson.Gson;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CallbackService {
    private final CallbackRepository callbackRepository;
    private final Gson gson;

    public CallbackService(CallbackRepository callbackRepository) {
        this.callbackRepository = callbackRepository;
        this.gson = new Gson();
    }

    public <T> UUID saveCallback(Callback<T> callback) {
        UUID id = UUID.randomUUID();
        String payload = gson.toJson(callback.payload());
        CallbackEntity entity = new CallbackEntity(id, callback.callbackType(), payload);
        callbackRepository.save(entity);
        return id;
    }

    public CallbackType getCallbackType(@NonNull UUID id) {
        return callbackRepository.findById(id)
            .map(CallbackEntity::getType)
            .orElse(null);
    }

    public <T> Callback<T> getCallback(@NonNull UUID id, Class<T> tClass) {
        return callbackRepository.findById(id)
            .map(it -> new Callback<>(it.getType(), gson.fromJson(it.getPayload(), tClass)))
            .orElse(null);
    }
}
