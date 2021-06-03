package io.github.ph1lou.werewolfplugin.commands.utilities;

import io.github.ph1lou.werewolfapi.ICommand;
import io.github.ph1lou.werewolfapi.WereWolfAPI;
import io.github.ph1lou.werewolfapi.registers.ConfigRegister;
import io.github.ph1lou.werewolfplugin.RegisterManager;
import org.bukkit.entity.Player;

public class CommandRules implements ICommand {

    @Override
    public void execute(WereWolfAPI game, Player player, String[] args) {

        for (ConfigRegister configRegister : RegisterManager.get().getConfigsRegister()) {

            if (configRegister.isAppearInMenu()) {
                if (game.getConfig().isConfigActive(configRegister.getKey())) {
                    player.sendMessage(game.translate("werewolf.utils.enable") + game.translate(configRegister.getKey()));
                } else {
                    player.sendMessage(game.translate("werewolf.utils.disable") + game.translate(configRegister.getKey()));
                }
            }

        }
    }
}
