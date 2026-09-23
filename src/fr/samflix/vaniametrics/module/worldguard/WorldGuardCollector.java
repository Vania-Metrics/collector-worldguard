package fr.samflix.vaniametrics.module.worldguard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.protection.events.DisallowedPVPEvent;
import com.sk89q.worldguard.protection.managers.RegionManager;

import fr.samflix.vaniametrics.api.Collector;
import fr.samflix.vaniametrics.api.Counter;
import fr.samflix.vaniametrics.api.Gauge;
import fr.samflix.vaniametrics.api.MetricRegistry;

/**
 * WorldGuard — les régions protégées, et ce qu'elles refusent.
 *
 * <p>DEUX NATURES DANS UN MÊME MODULE, et c'est assumé : le NOMBRE de régions est un état, qu'on
 * relève ; le PvP refusé est un événement, qu'on compte. Les séparer ferait deux jars pour un même
 * plugin, ce que l'organisation « un jar par plugin » interdit.
 *
 * <p>Le compte de régions se relève en fond : {@code RegionManager.size()} est bon marché, mais il
 * faut parcourir les mondes, et une région créée il y a dix secondes n'intéresse personne à la
 * seconde près.
 */
public final class WorldGuardCollector implements Collector, Listener {

	private Gauge regions;
	private Counter pvpRefuse;

	@Override
	public String nom() {
		return "region";
	}

	@Override
	public String origine() {
		return "WorldGuard";
	}

	@Override
	public boolean enFond() {
		return true;
	}

	@Override
	public long intervalleSecondes() {
		return 60;
	}

	@Override
	public void declarer(MetricRegistry r) {
		regions = r.gauge("region_count", "Régions protégées déclarées, par monde.", "world");
		pvpRefuse = r.counter("region_pvp_denied_total",
				"Attaques entre joueurs refusées par une région. C'est un compteur de FRICTION : "
						+ "s'il monte, des joueurs se battent là où ils ne peuvent pas.");
	}

	/**
	 * Le compte de régions, monde par monde.
	 *
	 * <p>{@code getLoaded()} ET PAS {@code get(World)}, et ce n'est pas une préférence : la
	 * seconde exige un {@code com.sk89q.worldedit.world.World}, donc WorldEdit au classpath de
	 * compilation — or WorldEdit 7.4.5 est compilé en CLASSE 69, c'est-à-dire Java 25, qu'un
	 * javac visant Java 21 refuse de lire. {@code getLoaded()} rend directement les gestionnaires
	 * chargés, et {@code RegionManager.getName()} donne le nom du monde : aucune dépendance de
	 * plus, et le résultat est le même.
	 */
	@Override
	public void relever(MetricRegistry r) {
		// Les mondes vont et viennent : sans remise à zéro, un monde déchargé garderait son
		// compte publié pour toujours.
		regions.clear();
		for (RegionManager gestionnaire :
				WorldGuard.getInstance().getPlatform().getRegionContainer().getLoaded()) {
			regions.set(gestionnaire.size(), gestionnaire.getName());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPvpRefuse(DisallowedPVPEvent e) {
		pvpRefuse.inc();
	}
}
