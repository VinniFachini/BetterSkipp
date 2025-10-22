package com.betterskipp.mixin.client;

import net.minecraft.village.Merchant;
import net.minecraft.screen.MerchantScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantScreenHandler.class)
public interface MerchantScreenHandlerAccessor {
    @Accessor("merchant")
    Merchant getMerchant();  // Merchant, não MerchantEntity
}