package com.betterskipp.server;

import com.betterskipp.mixin.accessor.MerchantScreenHandlerAccessor;
import com.betterskipp.mixin.accessor.VillagerEntityInvoker;
import com.betterskipp.network.CheckRefreshPermissionPayload;
import com.betterskipp.network.RefreshPermissionResponsePayload;
import com.betterskipp.network.RefreshTradesPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.Merchant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshTradesHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("BetterSkipp");

    public static void register() {
        PayloadTypeRegistry.playC2S().register(RefreshTradesPayload.ID, RefreshTradesPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CheckRefreshPermissionPayload.ID, CheckRefreshPermissionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RefreshPermissionResponsePayload.ID, RefreshPermissionResponsePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CheckRefreshPermissionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                handleCheckPermission(context.player(), payload.syncId());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(RefreshTradesPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                handleRefreshTrades(context.player(), payload.syncId());
            });
        });
    }

    private static void handleCheckPermission(ServerPlayerEntity player, int syncId) {
        ScreenHandler handler = player.currentScreenHandler;

        if (handler == null || handler.syncId != syncId || !(handler instanceof MerchantScreenHandler merchantHandler)) {
            ServerPlayNetworking.send(player, new RefreshPermissionResponsePayload(false));
            return;
        }

        Merchant merchant = ((MerchantScreenHandlerAccessor) merchantHandler).getMerchant();

        if (!(merchant instanceof VillagerEntity villager)) {
            ServerPlayNetworking.send(player, new RefreshPermissionResponsePayload(false));
            return;
        }

        var offers = villager.getOffers();

        if (offers == null || offers.isEmpty()) {
            ServerPlayNetworking.send(player, new RefreshPermissionResponsePayload(false));
            return;
        }

        // Verificar experiência do villager (se > 0, já teve trades)
        if (villager.getExperience() > 0) {
            ServerPlayNetworking.send(player, new RefreshPermissionResponsePayload(false));
            return;
        }

        // Verificar nível do villager (deve ser Novice)
        if (villager.getVillagerData().level() > 1) {
            ServerPlayNetworking.send(player, new RefreshPermissionResponsePayload(false));
            return;
        }

        // Verificar quantidade de ofertas (Novice tem no máximo 2)
        if (offers.size() > 2) {
            ServerPlayNetworking.send(player, new RefreshPermissionResponsePayload(false));
            return;
        }

        // Verificar se alguma trade foi usada
        boolean anyUsed = false;
        for (var offer : offers) {
            if (offer.hasBeenUsed() || offer.getUses() > 0) {
                anyUsed = true;
                break;
            }
        }

        ServerPlayNetworking.send(player, new RefreshPermissionResponsePayload(!anyUsed));
    }

    private static void handleRefreshTrades(ServerPlayerEntity player, int syncId) {
        ScreenHandler handler = player.currentScreenHandler;

        if (handler == null || handler.syncId != syncId) {
            LOGGER.warn("Tentativa de refresh com handler inválido do jogador {}", player.getName().getString());
            return;
        }

        if (!(handler instanceof MerchantScreenHandler merchantHandler)) {
            LOGGER.warn("Tentativa de refresh em handler que não é MerchantScreenHandler");
            return;
        }

        Merchant merchant = ((MerchantScreenHandlerAccessor) merchantHandler).getMerchant();

        if (!(merchant instanceof VillagerEntity villager)) {
            LOGGER.warn("Tentativa de refresh em merchant que não é VillagerEntity");
            return;
        }

        var offers = villager.getOffers();

        if (offers == null || offers.isEmpty()) {
            LOGGER.warn("Tentativa de refresh com ofertas vazias");
            return;
        }

        // Verificações de segurança
        if (villager.getExperience() > 0) {
            LOGGER.warn("Tentativa de refresh bloqueada: villager já tem experiência ({})", villager.getExperience());
            return;
        }

        if (villager.getVillagerData().level() > 1) {
            LOGGER.warn("Tentativa de refresh bloqueada: villager não é Novice (nível {})", villager.getVillagerData().level());
            return;
        }

        if (offers.size() > 2) {
            LOGGER.warn("Tentativa de refresh bloqueada: villager tem mais de 2 ofertas ({})", offers.size());
            return;
        }

        boolean anyUsed = false;
        for (var offer : offers) {
            if (offer.hasBeenUsed() || offer.getUses() > 0) {
                anyUsed = true;
                break;
            }
        }

        if (anyUsed) {
            LOGGER.warn("Tentativa de refresh bloqueada: alguma trade já foi usada");
            return;
        }

        // Limpar e gerar novas ofertas
        offers.clear();
        ((VillagerEntityInvoker) villager).invokeFillRecipes();
        villager.sendOffers(player, villager.getDisplayName(), villager.getVillagerData().level());
    }
}