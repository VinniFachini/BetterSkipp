package com.betterskipp.client;

import com.betterskipp.network.RefreshPermissionResponsePayload;
import com.betterskipp.network.RefreshSuccessPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.MerchantScreenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class BetterSkippClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("BetterSkipp");
    private static Consumer<Boolean> responseCallback = null;

    @Override
    public void onInitializeClient() {
        LOGGER.info("BetterSkipp Client inicializado!");

        ClientPlayNetworking.registerGlobalReceiver(
                RefreshPermissionResponsePayload.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        if (responseCallback != null) {
                            responseCallback.accept(payload.canRefresh());
                        } else {
                            LOGGER.warn("Resposta de permissão recebida mas nenhum callback registrado");
                        }
                    });
                }
        );

        // Receber as novas ofertas e atualizar a tela sem fechar
        ClientPlayNetworking.registerGlobalReceiver(
                RefreshSuccessPayload.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        MinecraftClient client = context.client();
                        if (client.player != null && client.player.currentScreenHandler instanceof MerchantScreenHandler handler) {
                            handler.setOffers(payload.offers());
                        }
                    });
                }
        );
    }

    public static void setResponseCallback(Consumer<Boolean> callback) {
        responseCallback = callback;
    }
}