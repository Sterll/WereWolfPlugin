package fr.ph1lou.werewolfplugin.commands.roles.villager.info.fox;

import fr.ph1lou.werewolfapi.annotations.RoleCommand;
import fr.ph1lou.werewolfapi.commands.ICommand;
import fr.ph1lou.werewolfapi.basekeys.Prefix;
import fr.ph1lou.werewolfapi.basekeys.RoleBase;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.roles.fox.BeginSniffEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.interfaces.ILimitedUse;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.interfaces.IProgress;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfplugin.roles.villagers.Fox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

@RoleCommand(key = "werewolf.role.fox.command",
        roleKeys = RoleBase.FOX,
        requiredPower = true,
        argNumbers = 1)
public class CommandFox implements ICommand {

    @Override
    public void execute(WereWolfAPI game, Player player, String[] args) {

        UUID uuid = player.getUniqueId();
        IPlayerWW playerWW = game.getPlayerWW(uuid).orElse(null);

        if (playerWW == null) return;

        IRole fox = playerWW.getRole();

        Player playerArg = Bukkit.getPlayer(args[0]);

        if (playerArg == null) {
            playerWW.sendMessageWithKey(Prefix.RED , "werewolf.check.offline_player");
            return;
        }
        UUID argUUID = playerArg.getUniqueId();
        IPlayerWW playerWW1 = game.getPlayerWW(argUUID).orElse(null);

        if (argUUID.equals(uuid)) {
            playerWW.sendMessageWithKey(Prefix.RED , "werewolf.check.not_yourself");
            return;
        }

        if (playerWW1 == null || !playerWW1.isState(StatePlayer.ALIVE)) {
            playerWW.sendMessageWithKey(Prefix.RED , "werewolf.check.player_not_found");
            return;
        }

        if (((ILimitedUse) fox).getUse() >= game.getConfig().getValue(Fox.SMELL_NUMBER)) {
            playerWW.sendMessageWithKey(Prefix.RED , "werewolf.check.power");
            return;
        }

        Location location = player.getLocation();
        Location locationTarget = playerArg.getLocation();

        if (player.getWorld().equals(playerArg.getWorld())) {
            if (location.distance(locationTarget) > game.getConfig().getValue(Fox.DISTANCE)) {
                playerWW.sendMessageWithKey(Prefix.RED , "werewolf.role.fox.not_enough_near");
                return;
            }
        } else {
            return;
        }

        ((IPower) fox).setPower(false);
        ((ILimitedUse) fox).setUse(((ILimitedUse) fox).getUse() + 1);

        BeginSniffEvent beginSniffEvent = new BeginSniffEvent(playerWW, playerWW1);
        Bukkit.getPluginManager().callEvent(beginSniffEvent);

        if (beginSniffEvent.isCancelled()) {
            playerWW.sendMessageWithKey(Prefix.RED , "werewolf.check.cancel");
            return;
        }

        ((IAffectedPlayers) fox).clearAffectedPlayer();
        ((IAffectedPlayers) fox).addAffectedPlayer(playerWW1);
        ((IProgress) fox).setProgress(0f);

        playerWW.sendMessageWithKey(Prefix.YELLOW , "werewolf.role.fox.smell_beginning",
                Formatter.player(playerArg.getName()));
    }
}
