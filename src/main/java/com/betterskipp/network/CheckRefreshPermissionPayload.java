package com.betterskipp.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CheckRefreshPermissionPayload(int syncId) implements CustomPayload {
    public static final CustomPayload.Id<CheckRefreshPermissionPayload> ID =
            new CustomPayload.Id<>(Identifier.of("betterskipp", "check_refresh_permission"));

    public static final PacketCodec<PacketByteBuf, CheckRefreshPermissionPayload> CODEC =
            PacketCodec.of(CheckRefreshPermissionPayload::write, CheckRefreshPermissionPayload::new);

    public CheckRefreshPermissionPayload(PacketByteBuf buf) {
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