package com.betterskipp;

import com.betterskipp.network.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterSkipp implements ModInitializer {

    public static final String MOD_ID = "betterskipp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando BetterSkipp!");

        // Register payload types
        LOGGER.info("Registrando payload types...");
        PayloadTypeRegistry.playC2S().register(CheckRefreshPermissionPayload.ID, CheckRefreshPermissionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RefreshTradesPayload.ID, RefreshTradesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RefreshPermissionResponsePayload.ID, RefreshPermissionResponsePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RefreshSuccessPayload.ID, RefreshSuccessPayload.CODEC);
        LOGGER.info("Payload types registrados!");

        // Registra o handler do packet no servidor
        com.betterskipp.server.RefreshTradesHandler.register();

        LOGGER.info("BetterSkipp carregado com sucesso!");
    }
}