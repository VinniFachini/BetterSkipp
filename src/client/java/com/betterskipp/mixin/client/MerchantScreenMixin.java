package com.betterskipp.mixin.client;

import com.betterskipp.client.BetterSkippClient;
import com.betterskipp.network.CheckRefreshPermissionPayload;
import com.betterskipp.network.RefreshTradesPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> {

    @Unique
    private ButtonWidget betterskipp$refreshButton;

    @Unique
    private boolean betterskipp$initialized = false;

    @Unique
    private int betterskipp$tickCounter = 0;

    public MerchantScreenMixin(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Esperar alguns ticks após a tela estar completamente carregada
        if (!this.betterskipp$initialized) {
            this.betterskipp$tickCounter++;
            if (this.betterskipp$tickCounter >= 5) { // Esperar 5 frames
                this.betterskipp$initialized = true;
                this.betterskipp$setupButton();
            }
        }
    }

    @Unique
    private void betterskipp$setupButton() {
        // Definir callback que será chamado quando a resposta chegar
        BetterSkippClient.setResponseCallback(canRefresh -> {
            MinecraftClient client = MinecraftClient.getInstance();

            // Executar na thread principal do cliente
            if (client != null) {
                client.execute(() -> {
                    // Verificar se a tela ainda é válida
                    if (client.currentScreen != (Object) this) {
                        return;
                    }

                    // Remover botão existente
                    if (this.betterskipp$refreshButton != null) {
                        this.remove(this.betterskipp$refreshButton);
                        this.betterskipp$refreshButton = null;
                    }

                    // Adicionar botão se permitido
                    if (canRefresh) {
                        try {
                            // Posicionar no canto superior direito da janela do villager
                            int buttonX = this.x + 76;
                            int buttonY = this.y + 5;

                            this.betterskipp$refreshButton = ButtonWidget.builder(
                                            Text.literal("\uD83D\uDD04"),
                                            button -> this.betterskipp$onRefreshClick()
                                    )
                                    .dimensions(buttonX, buttonY, 10, 10)
                                    .build();

                            this.addDrawableChild(this.betterskipp$refreshButton);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        // Pedir ao servidor para verificar
        try {
            ClientPlayNetworking.send(new CheckRefreshPermissionPayload(this.handler.syncId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Unique
    private void betterskipp$onRefreshClick() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.player == null || client.world == null || this.handler == null) {
            return;
        }

        try {
            ClientPlayNetworking.send(new RefreshTradesPayload(this.handler.syncId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removed() {
        BetterSkippClient.setResponseCallback(null);
        this.betterskipp$initialized = false;
        this.betterskipp$tickCounter = 0;

        if (this.betterskipp$refreshButton != null) {
            this.remove(this.betterskipp$refreshButton);
            this.betterskipp$refreshButton = null;
        }

        super.removed();
    }
}