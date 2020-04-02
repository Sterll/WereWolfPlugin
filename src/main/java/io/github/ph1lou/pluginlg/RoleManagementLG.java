package io.github.ph1lou.pluginlg;

import io.github.ph1lou.pluginlg.enumlg.*;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class RoleManagementLG {
	
	private final MainLG main;
	final Random r = new Random(System.currentTimeMillis());

	public RoleManagementLG(MainLG main) {
		this.main=main;	
	}
	
	public void repartitionRolesLG() {
		
		List<String> players = new ArrayList<>(main.playerLG.keySet());
		List<RoleLG> config = new ArrayList<>();
		main.config.tool_switch.put(ToolLG.CHAT,false);
		main.config.role_count.put(RoleLG.VILLAGEOIS,main.config.role_count.get(RoleLG.VILLAGEOIS)+players.size()-main.score.getRole());

		for(RoleLG role:RoleLG.values()) {
			for(int i = 0; i<main.config.role_count.get(role); i++) {
				if(!role.equals(RoleLG.COUPLE)) {
					config.add(role);
				}
			}
		}

		while(!players.isEmpty()) {
			
			int n =(int) Math.floor(r.nextFloat()*players.size());
			String playername = players.get(n);
			PlayerLG plg = main.playerLG.get(playername);
			plg.setCamp(config.get(0).getCamp());
			plg.setPower(config.get(0).getPower());
			plg.setRole(config.get(0));
			recoverRolePower(playername);
			config.remove(0);	
			players.remove(n);
		}
		
		main.endlg.check_victory();
	}
	
	public void recoverRolePower(String playername) {
		
		if (Bukkit.getPlayer(playername)==null) return;
		
		Player player = Bukkit.getPlayer(playername);
		PlayerLG plg = main.playerLG.get(playername);
		
		plg.setKit(true);
		
		player.sendMessage(main.text.description.get(plg.getRole()));
		player.sendMessage(main.text.getText(43));

		for(PotionEffectType p:effect_recover(playername)) {
			player.addPotionEffect(new PotionEffect(p,Integer.MAX_VALUE,0,false,false));
		}
		
		for(ItemStack i:main.stufflg.role_stuff.get(plg.getRole())) {
			
			if(player.getInventory().firstEmpty()==-1) {
				player.getWorld().dropItem(player.getLocation(),i);
			}
			else {
				player.getInventory().addItem(i);
				player.updateInventory();
			}
		}
		if (plg.isRole(RoleLG.LOUP_GAROU_BLANC)) {
			player.setMaxHealth(30);
			player.setHealth(30);
			if(main.isDay(Day.NIGHT)) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE,-1,false,false));
			}
		}
		else if(plg.isRole(RoleLG.FRERE_SIAMOIS)){
			player.setMaxHealth(26);
		}
		else if ((plg.isRole(RoleLG.ASSASSIN) && !main.isDay(Day.NIGHT)) || (plg.isCamp(Camp.LG) && main.isDay(Day.NIGHT))) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,-1,false,false));
		}	
		else if (plg.isRole(RoleLG.ENFANT_SAUVAGE)) {
			player.sendMessage(String.format(main.text.powerUse.get(RoleLG.ENFANT_SAUVAGE),main.score.conversion(main.config.value.get(TimerLG.MASTER_DURATION))));
		}
		else if (plg.isRole(RoleLG.CUPIDON)) {
			player.sendMessage(String.format(main.text.powerUse.get(RoleLG.CUPIDON),main.score.conversion(main.config.value.get(TimerLG.COUPLE_DURATION))));
		}
		else if (plg.isRole(RoleLG.ANGE)) {
			player.sendMessage(String.format(main.text.powerUse.get(RoleLG.ANGE),main.score.conversion(main.config.value.get(TimerLG.ANGE_DURATION))));
		}
		if(plg.isRole(RoleLG.ANGE_DECHU) || plg.isRole(RoleLG.ANGE_GARDIEN) || plg.isRole(RoleLG.ANGE)) {
			player.setMaxHealth(24);
			player.setHealth(24);
		}
	}

	
	public List<PotionEffectType> effect_recover(String playername) {
		
		List <PotionEffectType> effect = new ArrayList<>();
		
		if (main.playerLG.get(playername).isRole(RoleLG.VOYANTE) || main.playerLG.get(playername).isRole(RoleLG.VOYANTE_BAVARDE) || main.playerLG.get(playername).isRole(RoleLG.PETITE_FILLE) || main.playerLG.get(playername).isCamp(Camp.LG)) {
			effect.add(PotionEffectType.NIGHT_VISION);
		}
		if((main.playerLG.get(playername).isRole(RoleLG.VOLEUR) || (main.playerLG.get(playername).isRole(RoleLG.ANCIEN))  && main.playerLG.get(playername).hasPower())){
			effect.add(PotionEffectType.DAMAGE_RESISTANCE);
		}
		if(main.playerLG.get(playername).isRole(RoleLG.MINEUR)){
			effect.add(PotionEffectType.FAST_DIGGING);
		}
		if (main.playerLG.get(playername).isRole(RoleLG.RENARD) || main.playerLG.get(playername).isRole(RoleLG.VILAIN_PETIT_LOUP)) {
			effect.add(PotionEffectType.SPEED);
		}
		if(main.config.scenario.get(ScenarioLG.CAT_EYES)){
			effect.add(PotionEffectType.NIGHT_VISION);
		}
		return effect;
	}
	
	public void thief_recover_role(String killername,String playername){
		
		RoleLG role = main.playerLG.get(playername).getRole();
		
		PlayerLG klg = main.playerLG.get(killername);
		PlayerLG plg = main.playerLG.get(playername);
		
		klg.setRole(role);
		klg.setThief(true);
		klg.setPower(plg.hasPower());
		klg.setUse(plg.getUse());
		
		if((plg.isCamp(Camp.LG) || plg.isRole(RoleLG.LOUP_GAROU_BLANC)) && !klg.isCamp(Camp.LG)) {
			newLG(killername);
		}
		else if(plg.isCamp(Camp.VILLAGE) && !klg.isCamp(Camp.LG)) {
			klg.setCamp(Camp.VILLAGE);
		}
		
		if (Bukkit.getPlayer(killername)!=null) {
			
			Player killer = Bukkit.getPlayer(killername);
			
			killer.sendMessage(String.format(main.text.powerHasBeenUse.get(RoleLG.VOLEUR),main.text.translateRole.get(role)));
			killer.sendMessage(main.text.getText(43));
			killer.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			
			if (klg.isRole(RoleLG.VILAIN_PETIT_LOUP) || klg.isRole(RoleLG.RENARD)) {
				killer.removePotionEffect(PotionEffectType.SPEED);
			}
			
			for(PotionEffectType p:effect_recover(killername)) {
				killer.addPotionEffect(new PotionEffect(p,Integer.MAX_VALUE,0,false,false));
			}
			
			if (klg.isRole(RoleLG.LOUP_GAROU_BLANC)) {
				killer.setMaxHealth(30);
				if(main.isDay(Day.NIGHT)) {
					killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE,-1,false,false));
				}
			}
			else if(plg.isRole(RoleLG.FRERE_SIAMOIS)){
				killer.setMaxHealth(26);
			}
			else if(klg.isRole(RoleLG.PETITE_FILLE) || klg.isRole(RoleLG.LOUP_PERFIDE)) {
				klg.setPower(true);
			}	
			
			if (klg.isRole(RoleLG.ENFANT_SAUVAGE)) {
				
				if(klg.hasPower()) {
					killer.sendMessage(String.format(main.text.powerUse.get(RoleLG.ENFANT_SAUVAGE),main.score.conversion(main.config.value.get(TimerLG.MASTER_DURATION))));
				}
				else {
					String mastername = plg.getAffectedPlayer().get(0);
					klg.clearAffectedPlayer();
					plg.clearAffectedPlayer();
					klg.addAffectedPlayer(mastername);
					main.playerLG.get(mastername).addDisciple(killername);
					main.playerLG.get(mastername).removeDisciple(playername);
					
					if(mastername.equals(killername)) {
						main.role_manage.newLG(killername);
					}
					else killer.sendMessage(String.format(main.text.powerHasBeenUse.get(RoleLG.ENFANT_SAUVAGE),mastername));
				}
			}
			if (klg.isRole(RoleLG.CUPIDON)) {
				
				if(klg.hasPower()) {
					killer.sendMessage(String.format(main.text.powerUse.get(RoleLG.CUPIDON),main.score.conversion(main.config.value.get(TimerLG.COUPLE_DURATION))));
				}
				else {
					klg.addAffectedPlayer(plg.getAffectedPlayer().get(0));
					klg.addAffectedPlayer(plg.getAffectedPlayer().get(1));
					plg.clearAffectedPlayer();
					killer.sendMessage(String.format(main.text.powerHasBeenUse.get(RoleLG.CUPIDON),klg.getAffectedPlayer().get(0),klg.getAffectedPlayer().get(1)));
				}
			}
			if ((klg.isRole(RoleLG.ASSASSIN) && !main.isDay(Day.NIGHT)) || (klg.isCamp(Camp.LG) && main.isDay(Day.NIGHT) )) {	
				killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE,-1,false,false));

			}
			if (klg.isRole(RoleLG.LOUP_FEUTRE)){
				klg.setPosterCamp(plg.getPosterCamp());
				klg.setPosterRole(plg.getPosterRole());
				killer.sendMessage(String.format(main.text.getText(15),main.text.translateRole.get(klg.getRole())));
			}
			if (klg.isRole(RoleLG.ANGE)) {
				killer.sendMessage(String.format(main.text.powerUse.get(RoleLG.ANGE),main.score.conversion(main.config.value.get(TimerLG.ANGE_DURATION))));
				killer.setMaxHealth(24);
			}
			if (klg.isRole(RoleLG.TUEUR_EN_SERIE)) {
				if (Bukkit.getPlayer(playername)!=null) {
					killer.setMaxHealth(Bukkit.getPlayer(playername).getMaxHealth());
					klg.addKLostHeart(plg.getLostHeart());
				}
			}
			else if (klg.isRole(RoleLG.ANGE_DECHU) || klg.isRole(RoleLG.ANGE_GARDIEN)){
				klg.clearAffectedPlayer();
				klg.addAffectedPlayer(plg.getAffectedPlayer().get(0));
				plg.clearAffectedPlayer();
				
				if(main.playerLG.get(klg.getAffectedPlayer().get(0)).isState(State.MORT) || klg.getAffectedPlayer().get(0).equals(killername)) {
					if(klg.isRole(RoleLG.ANGE_DECHU)) {
						killer.setMaxHealth(30);
					}
				}
				else {
					killer.setMaxHealth(24);
				}
				killer.sendMessage(String.format(main.text.powerHasBeenUse.get(klg.getRole()),klg.getAffectedPlayer().get(0)));

			}
			if(!plg.getCouple().isEmpty()) {
				
				for(String c:plg.getCouple()) {


					if(!klg.getCouple().contains(c)) {
						
						klg.addCouple(c);
						main.playerLG.get(c).addCouple(killername);
						main.playerLG.get(c).removeCouple(playername);
						if(Bukkit.getPlayer(c)!=null) {
							Player pc = Bukkit.getPlayer(c);
							pc.sendMessage(String.format(main.text.description.get(RoleLG.COUPLE),killername));
							pc.playSound(pc.getLocation(), Sound.SHEEP_SHEAR,1,20);
						}
						killer.sendMessage(String.format(main.text.description.get(RoleLG.COUPLE),c));
						killer.playSound(killer.getLocation(), Sound.SHEEP_SHEAR,1,20);
					}
				}
				if(!klg.getCouple().contains(killername)){
					plg.clearCouple();
				}
				
				for(String cup:main.playerLG.keySet()) {
					if(main.playerLG.get(cup).isRole(RoleLG.CUPIDON) && main.playerLG.get(cup).getAffectedPlayer().contains(playername)) {
						main.playerLG.get(cup).addAffectedPlayer(killername);
						main.playerLG.get(cup).removeAffectedPlayer(playername);
					}
				}
				main.couple_manage.thiefCoupleRange(killername,playername);
			}
		}
		main.death_manage.death(playername);
	}
	
	public void auto_master() {


		for(String playername:main.playerLG.keySet()) {

			if (main.playerLG.get(playername).isState(State.LIVING) && main.playerLG.get(playername).isRole(RoleLG.ENFANT_SAUVAGE) && main.playerLG.get(playername).hasPower()) {
				
				String mastername = autoSelect(r.nextFloat(),playername);
				main.playerLG.get(mastername).addDisciple(playername);
				main.playerLG.get(playername).addAffectedPlayer(mastername);
				main.playerLG.get(playername).setPower(false);
				if(Bukkit.getPlayer(playername) != null){
					Player player = Bukkit.getPlayer(playername);
					player.sendMessage(String.format(main.text.getText(47),mastername));
					player.playSound(player.getLocation(), Sound.BAT_IDLE,1,20);
				}
			}
		}
		
	}

	public void brotherLife() {

		int counter = 0;
		double health = 0;
		for (String p:main.playerLG.keySet()) {
			if (main.playerLG.get(p).isState(State.LIVING) && main.playerLG.get(p).isRole(RoleLG.FRERE_SIAMOIS) && Bukkit.getPlayer(p) != null) {
				Player c = Bukkit.getPlayer(p);
				counter++;
				health += c.getHealth() / c.getMaxHealth();
			}
		}
		health /= counter;
		for (String p:main.playerLG.keySet()) {
			if (main.playerLG.get(p).isState(State.LIVING) && main.playerLG.get(p).isRole(RoleLG.FRERE_SIAMOIS) && Bukkit.getPlayer(p) != null) {
				Player c = Bukkit.getPlayer(p);
				if(health * c.getMaxHealth()>10){
					if(health * c.getMaxHealth()+1<c.getHealth()){
						c.playSound(c.getLocation(), Sound.BURP,1,20);
					}
					c.setHealth(health * c.getMaxHealth());
				}
			}
		}
	}
	
	public void auto_ange() {

		for(String playername:main.playerLG.keySet()) {
			
			PlayerLG plg = main.playerLG.get(playername);
			if (plg.isState(State.LIVING)){
				if(plg.isRole(RoleLG.ANGE)){
					plg.setPower(false);
					if(r.nextBoolean()){
						plg.setRole(RoleLG.ANGE_DECHU);
					}
					else plg.setRole(RoleLG.ANGE_GARDIEN);
				}
				if (plg.isRole(RoleLG.ANGE_DECHU) || plg.isRole(RoleLG.ANGE_GARDIEN)) {

					String targetname = autoSelect(r.nextFloat(),playername);
					plg.addAffectedPlayer(targetname);
					main.playerLG.get(targetname).addTargetOf(playername);

					if(Bukkit.getPlayer(playername) != null){
						Player player = Bukkit.getPlayer(playername);
						player.sendMessage(String.format(main.text.powerHasBeenUse.get(plg.getRole()),targetname));
						player.playSound(player.getLocation(), Sound.PORTAL_TRIGGER,1,20);
					}
				}
			}
		}
		if(!main.isState(StateLG.FIN)) {
			main.endlg.check_victory();
		}
	}
	
	public String autoSelect(float f, String playername) {
		
		List<String> players = new ArrayList<>();
		for(String p:main.playerLG.keySet()) {
			if(main.playerLG.get(p).isState(State.LIVING) && !p.equals(playername)) {
				players.add(p);
			}	
		}
		if(players.isEmpty()) {
			return playername;
		}
		return 	players.get((int) Math.floor(f*players.size()));
	}
	
	
	
	public void newLG(String playername) {
		
		if(main.config.tool_switch.get(ToolLG.LG_LIST) && main.config.value.get(TimerLG.LG_LIST)<0) {

			main.board.getTeam(playername).setPrefix("§4");
			main.playerLG.get(playername).setScoreBoard(main.board);

			for(String lgName : main.playerLG.keySet()) {
			
				if((main.playerLG.get(lgName).isCamp(Camp.LG) || main.playerLG.get(lgName).isRole(RoleLG.LOUP_GAROU_BLANC) )&& main.playerLG.get(lgName).isState(State.LIVING) && Bukkit.getPlayer(lgName)!=null ) {
					Player lg1 = Bukkit.getPlayer(lgName);
					lg1.sendMessage(main.text.getText(50));
					lg1.playSound(lg1.getLocation(),Sound.WOLF_HOWL, 1, 20);
				}
			}
		}
		
		if(!main.playerLG.get(playername).isRole(RoleLG.LOUP_GAROU_BLANC)) {
			main.playerLG.get(playername).setCamp(Camp.LG);
		}

		if(Bukkit.getPlayer(playername)!=null) {
			
			Player player = Bukkit.getPlayer(playername);
			player.setScoreboard(main.board);
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,Integer.MAX_VALUE,0,false,false));
			player.sendMessage(main.text.getText(51));
			player.playSound(player.getLocation(),Sound.WOLF_HOWL, 1, 20);
			if (main.isDay(Day.NIGHT)) {	
				player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE,-1,false,false));
			}
		}
	}
	
	public void lgList() {

		for(String lgName : main.playerLG.keySet()) {

			PlayerLG lg = main.playerLG.get(lgName);

			if((lg.isCamp(Camp.LG) || lg.isRole(RoleLG.LOUP_GAROU_BLANC)) && lg.isState(State.LIVING)) {

				main.board.getTeam(lgName).setPrefix("§4");
				lg.setScoreBoard(main.board);

				if(Bukkit.getPlayer(lgName)!=null) {
					Player player = Bukkit.getPlayer(lgName);
					player.sendMessage(main.text.getText(52));
					player.playSound(player.getLocation(),Sound.WOLF_HOWL, 1, 20);
					player.setScoreboard(main.board);
				}
			}	
		}
	}
}
