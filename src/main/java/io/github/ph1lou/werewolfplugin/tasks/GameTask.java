package io.github.ph1lou.werewolfplugin.tasks;


import io.github.ph1lou.werewolfapi.enumlg.Sounds;
import io.github.ph1lou.werewolfapi.enumlg.StateLG;
import io.github.ph1lou.werewolfapi.enumlg.TimerLG;
import io.github.ph1lou.werewolfapi.enumlg.ToolLG;
import io.github.ph1lou.werewolfapi.events.*;
import io.github.ph1lou.werewolfplugin.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class GameTask extends BukkitRunnable {

	private final GameManager game;
	int counter = 0;

	public GameTask(GameManager game) {
		this.game = game;
	}

	@Override
	public void run() {

		if (game.isState(StateLG.END)) {
			cancel();
			return;
		}

		counter++;
		for (Player p : Bukkit.getOnlinePlayers()) {
			game.getScore().actionBar(p);
		}
		if (counter % 4 != 0) return;

		World world = game.getWorld();
		WorldBorder wb = world.getWorldBorder();
		long time = world.getTime();
		Bukkit.getPluginManager().callEvent(new UpdateEvent());
		game.getOption().updateSelectionTimer();
		game.getOption().updateSelectionBorder();
		game.getScore().updateBoard();
		game.getLoversManage().detectionAmnesiacLover();
		game.getRoleManage().brotherLife();

		if (game.getConfig().getTimerValues().get(TimerLG.INVULNERABILITY) == 0) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage(game.translate("werewolf.announcement.invulnerability"));
				Sounds.GLASS.play(p);
			}

		}
		game.getConfig().getTimerValues().put(TimerLG.INVULNERABILITY, game.getConfig().getTimerValues().get(TimerLG.INVULNERABILITY) - 1);

		if (game.getConfig().getTimerValues().get(TimerLG.ROLE_DURATION) == 0) {

			game.setState(StateLG.GAME);
			game.getConfig().getConfigValues().put(ToolLG.CHAT, false);

			if (game.getConfig().isTrollSV()) {
				Bukkit.getPluginManager().callEvent(new TrollEvent());
			} else {
				for (Player p : Bukkit.getOnlinePlayers()) {
					Sounds.EXPLODE.play(p);
				}
				game.getRoleManage().repartitionRolesLG();
			}
		}
		game.getConfig().getTimerValues().put(TimerLG.ROLE_DURATION, game.getConfig().getTimerValues().get(TimerLG.ROLE_DURATION) - 1);

		if (game.getConfig().getTimerValues().get(TimerLG.PVP) == 0) {
			Bukkit.getPluginManager().callEvent(new PVPEvent());
		}
		game.getConfig().getTimerValues().put(TimerLG.PVP, game.getConfig().getTimerValues().get(TimerLG.PVP) - 1);

		if (game.getConfig().getTimerValues().get(TimerLG.ROLE_DURATION) < 0 && !game.getConfig().isTrollSV()) {

			if (game.getConfig().getTimerValues().get(TimerLG.MODEL_DURATION) == 0) {
				Bukkit.getPluginManager().callEvent(new AutoModelEvent());
			}
			game.getConfig().getTimerValues().put(TimerLG.MODEL_DURATION, game.getConfig().getTimerValues().get(TimerLG.MODEL_DURATION) - 1);

			if (game.getConfig().getTimerValues().get(TimerLG.LOVER_DURATION) == 0) {
				Bukkit.getPluginManager().callEvent(new LoversRepartitionEvent());

			}
			game.getConfig().getTimerValues().put(TimerLG.LOVER_DURATION, game.getConfig().getTimerValues().get(TimerLG.LOVER_DURATION) - 1);

			if (game.getConfig().getTimerValues().get(TimerLG.ANGEL_DURATION) == 0) {
				Bukkit.getPluginManager().callEvent(new AutoAngelEvent());
			}
			game.getConfig().getTimerValues().put(TimerLG.ANGEL_DURATION, game.getConfig().getTimerValues().get(TimerLG.ANGEL_DURATION) - 1);

			if (game.getConfig().getTimerValues().get(TimerLG.WEREWOLF_LIST) == 0) {
				Bukkit.getPluginManager().callEvent(new WereWolfListEvent());

			}
			game.getConfig().getTimerValues().put(TimerLG.WEREWOLF_LIST, game.getConfig().getTimerValues().get(TimerLG.WEREWOLF_LIST) - 1);
		}


		if (game.getConfig().getTimerValues().get(TimerLG.BORDER_BEGIN) == 0) {

			if (wb.getSize() != game.getConfig().getBorderMin()) {
				Bukkit.getPluginManager().callEvent(new BorderStartEvent());
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.sendMessage(game.translate("werewolf.announcement.border"));
					Sounds.FIREWORK_LAUNCH.play(p);
				}
			}
		} else if (game.getConfig().getTimerValues().get(TimerLG.BORDER_BEGIN) < 0) {
			game.getConfig().setBorderMax((int) (wb.getSize()));
			if(game.getConfig().getBorderMax()==game.getConfig().getBorderMin()){
				Bukkit.getPluginManager().callEvent(new BorderStopEvent());
			}
			else wb.setSize(game.getConfig().getBorderMin(), (long) Math.abs(wb.getSize() - game.getConfig().getBorderMin())* game.getConfig().getTimerValues().get(TimerLG.BORDER_DURATION) / 100);
		}
		game.getConfig().getTimerValues().put(TimerLG.BORDER_BEGIN, game.getConfig().getTimerValues().get(TimerLG.BORDER_BEGIN) - 1);

		if (game.getConfig().getTimerValues().get(TimerLG.DIGGING) == 0) {
			Bukkit.getPluginManager().callEvent(new DiggingEndEvent());

		}
		game.getConfig().getTimerValues().put(TimerLG.DIGGING, game.getConfig().getTimerValues().get(TimerLG.DIGGING) - 1);


		game.getConfig().getTimerValues().put(TimerLG.VOTE_BEGIN, game.getConfig().getTimerValues().get(TimerLG.VOTE_BEGIN) - 1);


		world.setTime((long) (time + 20 * (600f / game.getConfig().getTimerValues().get(TimerLG.DAY_DURATION) - 1)));

		game.getScore().addTimer();
	}

}

