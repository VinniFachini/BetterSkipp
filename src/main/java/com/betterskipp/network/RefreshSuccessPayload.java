package com.betterskipp.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOfferList;

public record RefreshSuccessPayload(TradeOfferList offers) implements CustomPayload {

    public static final CustomPayload.Id<RefreshSuccessPayload> ID =
            new CustomPayload.Id<>(Identifier.of("betterskipp", "refresh_success"));

    public static final PacketCodec<PacketByteBuf, RefreshSuccessPayload> CODEC =
            PacketCodec.of(RefreshSuccessPayload::write, RefreshSuccessPayload::new);

    public RefreshSuccessPayload(PacketByteBuf buf) {
        this((RegistryByteBuf) buf);
    }

    private RefreshSuccessPayload(RegistryByteBuf buf) {
        this(TradeOfferList.PACKET_CODEC.decode(buf));
    }

    public void write(PacketByteBuf buf) {
        RegistryByteBuf registryBuf = (RegistryByteBuf) buf;
        TradeOfferList.PACKET_CODEC.encode(registryBuf, this.offers);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}