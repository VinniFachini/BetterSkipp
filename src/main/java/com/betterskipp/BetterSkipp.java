package com.betterskipp;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterSkipp implements ModInitializer {

    public static final String MOD_ID = "betterskipp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando BetterSkipp!");

        // Registra o handler do packet no servidor
        com.betterskipp.server.RefreshTradesHandler.register();

        LOGGER.info("BetterSkipp carregado com sucesso!");
    }
}