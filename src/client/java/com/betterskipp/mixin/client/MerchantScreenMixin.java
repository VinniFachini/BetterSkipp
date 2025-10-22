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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> {

    @Unique
    private ButtonWidget refreshButton;

    private MerchantScreenMixin(MerchantScreenHandler handler, Text title) {
        super(handler, null, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addRefreshButton(CallbackInfo ci) {
        // Remover botão anterior se existir
        if (this.refreshButton != null) {
            this.remove(this.refreshButton);
            this.refreshButton = null;
        }

        // Definir callback que será chamado quando a resposta chegar
        BetterSkippClient.setResponseCallback(canRefresh -> {
            // Remover botão existente antes de decidir
            if (this.refreshButton != null) {
                this.remove(this.refreshButton);
                this.refreshButton = null;
            }

            // Adicionar botão APENAS se canRefresh for TRUE
            if (canRefresh) {
                int buttonX = this.x + 91;
                int buttonY = this.y + 5;

                this.refreshButton = ButtonWidget.builder(
                                Text.literal("🔄"),
                                button -> this.onRefreshClick()
                        )
                        .dimensions(buttonX, buttonY, 10, 10)
                        .build();

                this.addDrawableChild(this.refreshButton);
            }
        });

        // Pedir ao servidor para verificar
        ClientPlayNetworking.send(new CheckRefreshPermissionPayload(this.handler.syncId));
    }

    @Unique
    private void onRefreshClick() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null || this.handler == null) {
            return;
        }

        ClientPlayNetworking.send(new RefreshTradesPayload(this.handler.syncId));

        // NÃO remover o botão - ele continua disponível para novos refreshes
    }
}