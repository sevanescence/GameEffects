package com.makotomiyamoto.gameeffects;

import com.makotomiyamoto.gameeffects.command.CreateTestCircle;
import org.bukkit.plugin.java.JavaPlugin;

public final class GameEffects extends JavaPlugin {

    @Override
    public void onEnable() {

        registerCommands();

        this.getServer().getConsoleSender().sendMessage("GameEffects enabled!");

    }

    private void registerCommands() {

        this.getCommand("CreateTestCircle").setExecutor(new CreateTestCircle(this));

    }

}
