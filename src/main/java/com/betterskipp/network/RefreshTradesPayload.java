package com.betterskipp.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RefreshTradesPayload(int syncId) implements CustomPayload {
    public static final CustomPayload.Id<RefreshTradesPayload> ID =
            new CustomPayload.Id<>(Identifier.of("betterskipp", "refresh_trades"));

    public static final PacketCodec<PacketByteBuf, RefreshTradesPayload> CODEC =
            PacketCodec.of(RefreshTradesPayload::write, RefreshTradesPayload::new);

    public RefreshTradesPayload(PacketByteBuf buf) {
        this(buf.readInt());
    }

    public void write(PacketByteBuf buf) {
        buf.writeInt(this.syncId);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}