package com.wem.geezer.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        for (int i = 0; i < event.lines().size(); i++) {
            Component line = event.line(i);
            if (line == null) {
                continue;
            }

            String text = PlainTextComponentSerializer.plainText().serialize(line);
            Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
            event.line(i, coloredLine);
        }
    }
}
