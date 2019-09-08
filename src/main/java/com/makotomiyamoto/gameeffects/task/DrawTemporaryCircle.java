package com.makotomiyamoto.gameeffects.task;

import com.makotomiyamoto.gameeffects.GameEffects;
import com.makotomiyamoto.gameeffects.antivirusdev.CircleGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.HashSet;

public final class DrawTemporaryCircle implements Runnable {

    private Location center;
    private int radius, decrementRadii;
    private GameEffects api;

    public DrawTemporaryCircle(Location center, int radius, int decrementRadii, GameEffects api) {
        this.center = center;
        this.radius = radius;
        this.decrementRadii = decrementRadii;
        this.api = api;
    }

    @Override
    public void run() {

        api.getServer().broadcastMessage(ChatColor.AQUA + "Drawing circle...");

        HashSet<Location> circle = new HashSet<>();

        do {

            circle.addAll(CircleGenerator.generateCircle(center, radius - decrementRadii,
                    CircleGenerator.Plane.XZ, true, true));

            decrementRadii--;

        } while (decrementRadii > 0);

        HashMap<Location, Material> circleBlockCache = new HashMap<>();

        for (Location loc : circle) {
            circleBlockCache.put(loc, loc.getBlock().getType());
            loc.getBlock().setType(Material.AIR);
        }

        try {
            wait(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Location loc : circleBlockCache.keySet()) {
            loc.getBlock().setType(circleBlockCache.get(loc));
        }

        api.getServer().broadcastMessage(ChatColor.GREEN + "Circle reiterated!");

    }

}
