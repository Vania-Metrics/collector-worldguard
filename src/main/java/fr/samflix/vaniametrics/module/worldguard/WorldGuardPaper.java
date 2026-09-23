package fr.samflix.vaniametrics.module.worldguard;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import fr.samflix.vaniametrics.api.VaniaMetrics;
import fr.samflix.vaniametrics.api.VaniaMetricsProvider;

/**
 * WorldGuard region metrics.
 *
 * <p>Two kinds in one module: region count is polled, denied PvP is counted.
 */
public final class WorldGuardPaper extends JavaPlugin {

	private WorldGuardCollector collector;

	@Override
	public void onEnable() {
		VaniaMetrics metrics = VaniaMetricsProvider.get();
		collector = new WorldGuardCollector();
		metrics.register(collector);
		Bukkit.getPluginManager().registerEvents(collector, this);
	}

	@Override
	public void onDisable() {
		if (collector != null) {
			VaniaMetricsProvider.find().ifPresent(m -> m.unregister(collector));
		}
	}
}
