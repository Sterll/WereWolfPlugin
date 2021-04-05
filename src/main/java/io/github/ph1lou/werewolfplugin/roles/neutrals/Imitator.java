package io.github.ph1lou.werewolfplugin.roles.neutrals;

import io.github.ph1lou.werewolfapi.DescriptionBuilder;
import io.github.ph1lou.werewolfapi.GetWereWolfAPI;
import io.github.ph1lou.werewolfapi.ILover;
import io.github.ph1lou.werewolfapi.IPlayerWW;
import io.github.ph1lou.werewolfapi.enums.StateGame;
import io.github.ph1lou.werewolfapi.enums.StatePlayer;
import io.github.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import io.github.ph1lou.werewolfapi.events.game.day_cycle.NightEvent;
import io.github.ph1lou.werewolfapi.events.game.life_cycle.FirstDeathEvent;
import io.github.ph1lou.werewolfapi.events.game.life_cycle.SecondDeathEvent;
import io.github.ph1lou.werewolfapi.events.roles.StealEvent;
import io.github.ph1lou.werewolfapi.events.werewolf.NewWereWolfEvent;
import io.github.ph1lou.werewolfapi.rolesattributs.IAffectedPlayers;
import io.github.ph1lou.werewolfapi.rolesattributs.IPower;
import io.github.ph1lou.werewolfapi.rolesattributs.IRole;
import io.github.ph1lou.werewolfapi.rolesattributs.RoleNeutral;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Imitator extends RoleNeutral implements IAffectedPlayers, IPower {

    private final List<IPlayerWW> affectedPlayer = new ArrayList<>();
    private boolean power = true;

    public Imitator(GetWereWolfAPI main, IPlayerWW playerWW, String key) {
        super(main, playerWW, key);
    }

    @Override
    public @NotNull String getDescription() {

        return new DescriptionBuilder(game, this)
                .setDescription(() -> game.translate("werewolf.role.imitator.description"))
                .setEffects(() -> game.translate("werewolf.role.imitator.effect"))
                .build();
    }


    @Override
    public void setPower(boolean power) {
        this.power = power;
    }

    @Override
    public boolean hasPower() {
        return (this.power);
    }

    @Override
    public void addAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.add(playerWW);
    }

    @Override
    public void removeAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.remove(playerWW);
    }

    @Override
    public void clearAffectedPlayer() {
        this.affectedPlayer.clear();
    }

    @Override
    public List<IPlayerWW> getAffectedPlayers() {
        return (this.affectedPlayer);
    }

    @EventHandler
    public void onDay(DayEvent event) {
        restoreStrength();
    }

    @EventHandler
    public void onNight(NightEvent event) {
        restoreStrength();
    }


    private void restoreStrength() {

        if (!hasPower()) return;

        if (!getPlayerWW().isState(StatePlayer.ALIVE)) return;

        getPlayerWW().addPotionEffect(PotionEffectType.INCREASE_DAMAGE);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {

        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();

        if (!killer.getUniqueId().equals(getPlayerUUID())) return;

        killer.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                1200,
                0,
                false,
                false));

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFirstDeathEvent(FirstDeathEvent event) {

        IPlayerWW playerWW = event.getPlayerWW();

        if (!playerWW.getLastKiller().isPresent()) return;

        if (!playerWW.getLastKiller().get().equals(getPlayerWW())) return;

        if (!hasPower()) return;

        event.setCancelled(true);

        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin) main, () -> {
            if (!game.isState(StateGame.END)) {
                if (getPlayerWW().isState(StatePlayer.ALIVE)
                        && hasPower()) {
                    imitatorRecoverRole(playerWW);
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin) main, () -> {
                        if (!game.isState(StateGame.END)) {
                            Bukkit.getPluginManager().callEvent(
                                    new SecondDeathEvent(playerWW));
                        }

                    }, 20L);
                }
            }
        }, 7 * 20);
    }


    public void imitatorRecoverRole(IPlayerWW playerWW) {

        IRole role = playerWW.getRole();

        setPower(false);

        HandlerList.unregisterAll((Listener) getPlayerWW().getRole());
        IRole roleClone = role.publicClone();
        getPlayerWW().setRole(roleClone);
        assert roleClone != null;
        Bukkit.getPluginManager().registerEvents((Listener) roleClone, (Plugin) main);
        if (this.getInfected()) {
            roleClone.setInfected();
        } else if (roleClone.isWereWolf()) {
            Bukkit.getPluginManager().callEvent(new NewWereWolfEvent(getPlayerWW()));
        }
        roleClone.setTransformedToNeutral(true);
        roleClone.setDeathRole(this.getKey());

        getPlayerWW().sendMessageWithKey("werewolf.role.thief.realized_theft",
                game.translate(role.getKey()));
        getPlayerWW().sendMessageWithKey("werewolf.role.thief.details");

        getPlayerWW().removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        Bukkit.getPluginManager().callEvent(new StealEvent(getPlayerWW(),
                playerWW,
                roleClone.getKey()));

        roleClone.recoverPower();
        roleClone.recoverPotionEffect();

        for (int i = 0; i < playerWW.getLovers().size(); i++) {
            ILover ILover = playerWW.getLovers().get(i);
            if (ILover.swap(playerWW, getPlayerWW())) {
                getPlayerWW().addLover(ILover);
                playerWW.removeLover(ILover);
                i--;
            }
        }

        game.death(playerWW);
    }


    @Override
    public void recoverPower() {
    }

    @Override
    public void recoverPotionEffect() {

        super.recoverPotionEffect();

        restoreStrength();
    }

}