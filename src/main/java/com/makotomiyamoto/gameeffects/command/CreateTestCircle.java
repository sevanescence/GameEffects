package com.makotomiyamoto.gameeffects.command;

import com.makotomiyamoto.gameeffects.GameEffects;
import com.makotomiyamoto.gameeffects.task.DrawTemporaryCircle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CreateTestCircle implements CommandExecutor {

    private GameEffects api;

    public CreateTestCircle(GameEffects api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 2) {
            sender.sendMessage("Usage: /CreateTestCircle <int_radius> <int_decrementRadii>");
            return false;
        }

        if (args[0].contains("[^0-9]") || args[1].contains("[^0-9]")) {
            sender.sendMessage(ChatColor.RED + "Arguments must be numerical. Usage:");
            sender.sendMessage("/CreateTestCircle <int_radius> <int_decrementRadii>");
            return false;
        }

        Player player = (Player) sender;

        DrawTemporaryCircle circleTask = new DrawTemporaryCircle(
                player.getLocation(),
                Integer.parseInt(args[0]),
                Integer.parseInt(args[1]),
                api
        );

        Bukkit.getScheduler().runTaskAsynchronously(api, circleTask);

        return true;

    }

}
