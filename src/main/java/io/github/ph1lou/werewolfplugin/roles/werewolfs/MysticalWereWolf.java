package io.github.ph1lou.werewolfplugin.roles.werewolfs;


import io.github.ph1lou.werewolfapi.DescriptionBuilder;
import io.github.ph1lou.werewolfapi.GetWereWolfAPI;
import io.github.ph1lou.werewolfapi.IPlayerWW;
import io.github.ph1lou.werewolfapi.enums.StatePlayer;
import io.github.ph1lou.werewolfapi.events.game.life_cycle.FinalDeathEvent;
import io.github.ph1lou.werewolfapi.events.roles.mystical_werewolf.MysticalWerewolfRevelationEvent;
import io.github.ph1lou.werewolfapi.events.werewolf.WereWolfChatEvent;
import io.github.ph1lou.werewolfapi.rolesattributs.RoleWereWolf;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class MysticalWereWolf extends RoleWereWolf {

    public MysticalWereWolf(GetWereWolfAPI main, IPlayerWW playerWW, String key) {
        super(main, playerWW, key);
    }


    @Override
    public @NotNull String getDescription() {
        return new DescriptionBuilder(game, this)
                .setDescription(() -> game.translate("werewolf.role.mystical_werewolf.description"))
                .setEffects(() -> game.translate("werewolf.description.werewolf"))
                .build();
    }


    @Override
    public void recoverPower() {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMessage(WereWolfChatEvent event) {

        if (!event.getPlayerWW().equals(getPlayerWW())) return;

        getPlayerWW().sendMessageWithKey("werewolf.role.mystical_werewolf.no_message");
        event.setCancelled(true);
    }

    @Override
    protected void openWereWolfChat() {
    }

    @EventHandler
    public void onWereWolfDeath(FinalDeathEvent event) {

        if (!getPlayerWW().isState(StatePlayer.ALIVE)) return;

        if (!event.getPlayerWW().getRole().isWereWolf()) return;

        List<IPlayerWW> roles = game.getPlayerWW()
                .stream().filter(playerWW -> playerWW.isState(StatePlayer.ALIVE))
                .filter(playerWW -> !playerWW.getRole().isWereWolf())
                .collect(Collectors.toList());

        if (roles.isEmpty()) return;

        IPlayerWW roleWW = roles.get((int) Math.floor(game.getRandom().nextFloat() * roles.size()));

        Bukkit.getPluginManager().callEvent(new MysticalWerewolfRevelationEvent(getPlayerWW(), roleWW));

        getPlayerWW().sendMessageWithKey("werewolf.role.mystical_werewolf.werewolf_death", roleWW.getName(), game.translate(roleWW.getRole().getKey()));
    }


}