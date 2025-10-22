package com.betterskipp.client;

import com.betterskipp.network.RefreshPermissionResponsePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class BetterSkippClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("BetterSkipp");
    private static Consumer<Boolean> responseCallback = null;

    @Override
    public void onInitializeClient() {
        LOGGER.info("BetterSkipp Client inicializando...");

        // Registrar handler para receber resposta do servidor
        ClientPlayNetworking.registerGlobalReceiver(RefreshPermissionResponsePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                LOGGER.info("📥 Pacote recebido do servidor: canRefresh = {}", payload.canRefresh());

                if (responseCallback != null) {
                    LOGGER.info("✅ Callback existe, executando...");
                    responseCallback.accept(payload.canRefresh());
                } else {
                    LOGGER.warn("⚠️ Callback é NULL - resposta ignorada!");
                }
            });
        });

        LOGGER.info("BetterSkipp Client inicializado com sucesso!");
    }

    public static void setResponseCallback(Consumer<Boolean> callback) {
        if (callback != null) {
            LOGGER.info("🔧 Callback registrado");
        } else {
            LOGGER.info("🔧 Callback removido");
        }
        responseCallback = callback;
    }
}