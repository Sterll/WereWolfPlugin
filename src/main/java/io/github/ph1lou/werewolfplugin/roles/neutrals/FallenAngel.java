package io.github.ph1lou.werewolfplugin.roles.neutrals;


import io.github.ph1lou.werewolfapi.IPlayerWW;
import io.github.ph1lou.werewolfapi.WereWolfAPI;
import io.github.ph1lou.werewolfapi.enums.AngelForm;
import io.github.ph1lou.werewolfapi.events.roles.angel.AngelChoiceEvent;
import org.bukkit.Bukkit;

public class FallenAngel extends Angel {

    public FallenAngel(WereWolfAPI api, IPlayerWW playerWW, String key) {
        super(api, playerWW, key);
        setChoice(AngelForm.FALLEN_ANGEL);
        Bukkit.getPluginManager().callEvent(
                new AngelChoiceEvent(this.getPlayerWW(), AngelForm.FALLEN_ANGEL));
    }
}
