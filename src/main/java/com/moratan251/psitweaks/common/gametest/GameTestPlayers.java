package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.registration.ChannelAttributes;

/** ワールドに入れる普通のプレイヤーと、チーム・ペットを用意する GameTest 用の道具。 */
final class GameTestPlayers {
    private GameTestPlayers() {
    }

    /**
     * 接続付きの普通の ServerPlayer（サバイバル）をワールドに入れる。FakePlayer と違ってダメージを受け、UUID での検索で見つかる。
     * 入った直後の 60 tick は無敵で、物理は動かない（{@code doTick} を呼ぶ接続処理がないため）。
     */
    static ServerPlayer inLevel(GameTestHelper helper, String name, Vec3 relative) {
        ServerLevel level = helper.getLevel();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        // ワールドに入ると他 Mod（Curios など）が同期の payload を送る。チャンネルを取り決めていない接続は NeoForge が
        // 送信を拒むので、どのチャンネルも受け付ける接続にする（送った payload はどこにも届かない。docs/pitfalls.md PIT-053）。
        connection.channel().attr(ChannelAttributes.ADHOC_CHANNELS).set(new AbstractSet<>() {
            @Override
            public boolean contains(Object channel) {
                return true;
            }

            @Override
            public Iterator<ResourceLocation> iterator() {
                return Collections.emptyIterator();
            }

            @Override
            public int size() {
                return 0;
            }
        });
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), connection, player,
                CommonListenerCookie.createInitial(profile, false));
        Vec3 at = helper.absoluteVec(relative);
        player.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        level.addNewPlayer(player);
        return player;
    }

    static void remove(GameTestHelper helper, ServerPlayer player) {
        if (!player.isRemoved()) {
            helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
        if (!(player instanceof FakePlayer)) {
            player.getAdvancements().stopListening();
        }
    }

    /** 最大体力を増やし、一撃で倒れないようにする。 */
    static <T extends LivingEntity> T tough(T entity, float health) {
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        entity.setHealth(health);
        return entity;
    }

    /** {@code owner} に飼われたオオカミ。動かず落ちない。 */
    static Wolf pet(GameTestHelper helper, UUID owner, Vec3 relative) {
        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, relative);
        wolf.setNoAi(true);
        wolf.setTame(true, false);
        wolf.setOwnerUUID(owner);
        return wolf;
    }

    /** スコアボードのチームを作り、名前の持ち主を入れる。後で {@link #removeTeam} で消す。 */
    static PlayerTeam team(GameTestHelper helper, String name, boolean friendlyFire, String... members) {
        var scoreboard = helper.getLevel().getScoreboard();
        PlayerTeam existing = scoreboard.getPlayerTeam(name);
        if (existing != null) {
            scoreboard.removePlayerTeam(existing);
        }
        PlayerTeam team = scoreboard.addPlayerTeam(name);
        team.setAllowFriendlyFire(friendlyFire);
        for (String member : members) {
            scoreboard.addPlayerToTeam(member, team);
        }
        return team;
    }

    static void removeTeam(GameTestHelper helper, PlayerTeam team) {
        var scoreboard = helper.getLevel().getScoreboard();
        if (scoreboard.getPlayerTeam(team.getName()) == team) {
            scoreboard.removePlayerTeam(team);
        }
    }
}
