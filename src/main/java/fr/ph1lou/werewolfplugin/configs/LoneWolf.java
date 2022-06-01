package fr.ph1lou.werewolfplugin.configs;

import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.annotations.Configuration;
import fr.ph1lou.werewolfapi.basekeys.ConfigBase;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.listeners.impl.ListenerWerewolf;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.basekeys.Prefix;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.basekeys.TimerBase;
import fr.ph1lou.werewolfapi.events.game.configs.LoneWolfEvent;
import fr.ph1lou.werewolfapi.events.game.life_cycle.FinalDeathEvent;
import fr.ph1lou.werewolfapi.events.game.timers.WereWolfListEvent;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.List;
import java.util.stream.Collectors;

@Configuration(key = ConfigBase.LONE_WOLF)
public class LoneWolf extends ListenerWerewolf {

    public LoneWolf(WereWolfAPI main) {
        super(main);
    }

    @EventHandler
    public void designAloneWolf(WereWolfListEvent event) {

        WereWolfAPI game = this.getGame();

        BukkitUtils.scheduleSyncDelayedTask(() -> {
            if (!game.isState(StateGame.END) && isRegister()) {
                this.designSolitary();
            }
        }, (long) (game.getRandom().nextFloat() * 3600 * 20));
    }

    private void designSolitary() {

        WereWolfAPI game = this.getGame();

        List<IRole> roleWWs = game.getPlayersWW().stream()
                .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE))
                .map(IPlayerWW::getRole)
                .filter(IRole::isWereWolf)
                .collect(Collectors.toList());

        if (roleWWs.isEmpty()) return;

        IRole role = roleWWs.get((int) Math.floor(game.getRandom().nextDouble() * roleWWs.size()));

        LoneWolfEvent event = new LoneWolfEvent((role.getPlayerWW()));

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        role.getPlayerWW().sendMessageWithKey(Prefix.RED , "werewolf.configurations.lone_wolf.message");

        if (role.getPlayerWW().getMaxHealth() < 30) {
            role.getPlayerWW().addPlayerMaxHealth(Math.min(8, 30 - role.getPlayerWW().getMaxHealth()));
        }
        role.setSolitary(true);
        register(false);
    }

    @EventHandler
    public void onDeath(FinalDeathEvent event) {

        WereWolfAPI game = this.getGame();

        if (game.getConfig().getTimerValue(TimerBase.WEREWOLF_LIST) > 0) return;

        designSolitary();
    }
}
