package com.betterskipp.mixin.client;

import com.betterskipp.client.BetterSkippClient;
import com.betterskipp.network.CheckRefreshPermissionPayload;
import com.betterskipp.network.RefreshTradesPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterSkipp");

    @Unique
    private ButtonWidget refreshButton;

    private MerchantScreenMixin(MerchantScreenHandler handler, Text title) {
        super(handler, null, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addRefreshButton(CallbackInfo ci) {
        LOGGER.info("=== INIT CALLED ===");

        // Remover botão anterior se existir
        if (this.refreshButton != null) {
            this.remove(this.refreshButton);
            this.refreshButton = null;
            LOGGER.info("Botão anterior removido");
        }

        // Definir callback que será chamado quando a resposta chegar
        BetterSkippClient.setResponseCallback(canRefresh -> {
            LOGGER.info("=== RESPOSTA RECEBIDA: canRefresh = {} ===", canRefresh);

            // Remover botão existente antes de decidir
            if (this.refreshButton != null) {
                this.remove(this.refreshButton);
                this.refreshButton = null;
            }

            // Adicionar botão APENAS se canRefresh for TRUE
            if (canRefresh) {
                LOGGER.info("✅ Adicionando botão de refresh");
                int buttonX = this.x + 91;
                int buttonY = this.y + 5;

                this.refreshButton = ButtonWidget.builder(
                                Text.literal("🔄"),
                                button -> this.onRefreshClick()
                        )
                        .dimensions(buttonX, buttonY, 10, 10)
                        .build();

                this.addDrawableChild(this.refreshButton);
            } else {
                LOGGER.info("❌ NÃO adicionando botão - alguma trade foi usada ou não há permissão");
            }
        });

        // Pedir ao servidor para verificar
        LOGGER.info("📤 Enviando pedido de verificação ao servidor (syncId: {})", this.handler.syncId);
        ClientPlayNetworking.send(new CheckRefreshPermissionPayload(this.handler.syncId));
    }

    @Unique
    private void onRefreshClick() {
        LOGGER.info("=== 🔄 BOTÃO CLICADO ===");
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null || this.handler == null) {
            LOGGER.warn("Cliente, player ou handler inválido");
            return;
        }

        LOGGER.info("📤 Enviando pedido de refresh ao servidor");
        ClientPlayNetworking.send(new RefreshTradesPayload(this.handler.syncId));

        // Remover o botão após clicar
        if (this.refreshButton != null) {
            this.remove(this.refreshButton);
            this.refreshButton = null;
            LOGGER.info("Botão removido após clique");
        }

        // Limpar callback
        BetterSkippClient.setResponseCallback(null);
    }
}