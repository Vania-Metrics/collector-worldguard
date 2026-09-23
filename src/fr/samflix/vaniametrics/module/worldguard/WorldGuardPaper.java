package fr.samflix.vaniametrics.module.worldguard;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import fr.samflix.vaniametrics.api.VaniaMetrics;
import fr.samflix.vaniametrics.api.VaniaMetricsProvider;

/**
 * Métriques des régions WorldGuard.
 *
 * <p>Deux natures dans un module : le compte de régions se relève, le PvP refusé se compte.
 */
public final class WorldGuardPaper extends JavaPlugin {

	private WorldGuardCollector collecteur;

	@Override
	public void onEnable() {
		VaniaMetrics metriques = VaniaMetricsProvider.get();
		collecteur = new WorldGuardCollector();
		metriques.enregistrer(collecteur);
		Bukkit.getPluginManager().registerEvents(collecteur, this);
	}

	@Override
	public void onDisable() {
		if (collecteur != null) {
			VaniaMetricsProvider.chercher().ifPresent(m -> m.retirer(collecteur));
		}
	}
}
