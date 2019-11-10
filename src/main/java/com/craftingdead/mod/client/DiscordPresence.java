package com.craftingdead.mod.client;

import lombok.RequiredArgsConstructor;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;

public class DiscordPresence {

  private static boolean initialized;

  public static void initialize(String clientId) {
    if (initialized) {
      return;
    }
    DiscordRPC.discordInitialize(clientId, new DiscordEventHandlers(), true, null);
    initialized = true;
  }

  public static void updateState(GameState state, ClientDist client) {
    if (!initialized) {
      return;
    }
    DiscordRichPresence presence = new DiscordRichPresence();
    state.applyState(presence, client);
    DiscordRPC.discordUpdatePresence(presence);
  }

  public static void shutdown() {
    if (!initialized) {
      return;
    }
    DiscordRPC.discordShutdown();
  }

  @RequiredArgsConstructor
  public static enum GameState {

    LOADING("presence.loading"), IDLE("presence.idle"), SINGLEPLAYER("presence.singleplayer"), LAN(
        "presence.lan"), MULTIPLAYER("presence.multiplayer") {
      @Override
      public void applyState(DiscordRichPresence presence, ClientDist client) {
        ServerData serverData = Minecraft.getInstance().getCurrentServerData();
        if (serverData != null) {
          presence.state = serverData.serverIP;
        }
      }
    };

    private final String translationKey;

    void applyState(DiscordRichPresence presence, ClientDist client) {
      presence.details = I18n.format(this.translationKey);
      presence.startTimestamp = System.currentTimeMillis() / 1000;
      presence.largeImageKey = "craftingdead";
      presence.largeImageText = "Crafting Dead";
    }
  }
}