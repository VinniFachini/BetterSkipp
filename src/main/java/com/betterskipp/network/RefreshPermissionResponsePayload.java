package com.betterskipp.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RefreshPermissionResponsePayload(boolean canRefresh) implements CustomPayload {
    public static final CustomPayload.Id<RefreshPermissionResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of("betterskipp", "refresh_permission_response"));

    public static final PacketCodec<PacketByteBuf, RefreshPermissionResponsePayload> CODEC =
            PacketCodec.of(RefreshPermissionResponsePayload::write, RefreshPermissionResponsePayload::new);

    public RefreshPermissionResponsePayload(PacketByteBuf buf) {
        this(buf.readBoolean());
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(this.canRefresh);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}