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
 * WorldGuard — protected regions, and what they deny.
 *
 * <p>Two kinds in one module, deliberately: region COUNT is state, polled; denied PvP is an
 * event, counted. Splitting them would mean two jars for one plugin, which the "one jar per
 * plugin" layout forbids.
 *
 * <p>Region count is polled in the background: {@code RegionManager.size()} is cheap, but it
 * requires walking every world, and a region created ten seconds ago doesn't matter to the
 * second.
 */
public final class WorldGuardCollector implements Collector, Listener {

	private Gauge regions;
	private Counter pvpDenied;

	@Override
	public String name() {
		return "region";
	}

	@Override
	public String source() {
		return "WorldGuard";
	}

	@Override
	public boolean isBackground() {
		return true;
	}

	@Override
	public long intervalSeconds() {
		return 60;
	}

	@Override
	public void declare(MetricRegistry r) {
		regions = r.gauge("region_count", "Declared protected regions, per world.", "world");
		pvpDenied = r.counter("region_pvp_denied_total",
				"Player-vs-player attacks denied by a region. A FRICTION counter: "
						+ "if it climbs, players are fighting where they can't.");
	}

	/**
	 * Region count, world by world.
	 *
	 * <p>{@code getLoaded()} and NOT {@code get(World)} — not a stylistic choice: the latter
	 * requires a {@code com.sk89q.worldedit.world.World}, hence WorldEdit on the compile
	 * classpath — but WorldEdit 7.4.5 is compiled at CLASS 69, i.e. Java 25, which a javac
	 * targeting Java 21 refuses to read. {@code getLoaded()} returns the loaded managers
	 * directly, and {@code RegionManager.getName()} gives the world name: no extra dependency,
	 * same result.
	 */
	@Override
	public void collect(MetricRegistry r) {
		// Worlds come and go: without clearing, an unloaded world would keep its last
		// published count forever.
		regions.clear();
		for (RegionManager manager :
				WorldGuard.getInstance().getPlatform().getRegionContainer().getLoaded()) {
			regions.set(manager.size(), manager.getName());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPvpDenied(DisallowedPVPEvent e) {
		pvpDenied.inc();
	}
}
