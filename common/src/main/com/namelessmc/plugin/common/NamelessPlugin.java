package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.event.NamelessEvent;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.kyori.event.EventBus;
import org.bstats.MetricsBase;
import org.bstats.charts.SimplePie;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NamelessPlugin {

	private final AbstractScheduler scheduler;
	private final ConfigurationHandler configuration;
	private final AbstractLogger logger;
	private final ApiProvider api;
	private final LanguageHandler language;
	private final DateFormatter dateFormatter;
	private final UserCache userCache;
	private final EventBus<NamelessEvent> eventBus;
	private final PropertiesManager propertiesManager;

	private final List<Reloadable> reloadables = new ArrayList<>();

	private AbstractAudienceProvider audienceProvider;

	public NamelessPlugin(final Path dataDirectory,
						  final AbstractScheduler scheduler,
						  final Function<ConfigurationHandler, AbstractLogger> loggerInstantiator,
						  final @Nullable Path logPath,
						  final String platformInternalName,
						  final String platformVersion) {
		this.scheduler = scheduler;

		this.configuration = this.registerReloadable(
				new ConfigurationHandler(dataDirectory)
		);
		this.logger = this.registerReloadable(
				loggerInstantiator.apply(this.configuration)
		);
		this.api = this.registerReloadable(
				new ApiProvider(scheduler, this.logger, this.configuration)
		);
		this.propertiesManager = new PropertiesManager(dataDirectory.resolve("nameless.dat"), this);
		this.language = this.registerReloadable(
				new LanguageHandler(dataDirectory, this.configuration, this.logger)
		);
		this.dateFormatter = this.registerReloadable(
				new DateFormatter(this.configuration));
		this.userCache = this.registerReloadable(
				new UserCache(this));

		this.eventBus = EventBus.create(NamelessEvent.class);

		this.registerReloadable(new AnnouncementTask(this));
		this.registerReloadable(new JoinNotificationsMessage(this));
		this.registerReloadable(new JoinNotRegisteredMessage(this));
		this.registerReloadable(new Metrics(this, platformInternalName, platformVersion));
		this.registerReloadable(new Store(this));
		this.registerReloadable(new SyncBanToWebsite(this));
		this.registerReloadable(new Websend(this, logPath));
	}

	public ConfigurationHandler config() {
		return this.configuration;
	}

	public AbstractLogger logger() {
		return this.logger;
	}

	public ApiProvider apiProvider() {
		return this.api;
	}

	public LanguageHandler language() {
		return this.language;
	}

	public DateFormatter dateFormatter() {
		return this.dateFormatter;
	}

	public AbstractScheduler scheduler() {
		return this.scheduler;
	}

	public AbstractAudienceProvider audiences() {
		return this.audienceProvider;
	}

	public UserCache userCache() {
		return this.userCache;
	}

	public @NonNull EventBus<NamelessEvent> events() {
		return this.eventBus;
	}

	public void setAudienceProvider(final @NonNull AbstractAudienceProvider audienceProvider) {
		this.audienceProvider = audienceProvider;
	}

	public PropertiesManager properties() {
		return this.propertiesManager;
	}

	public void reload() {
		for (Reloadable reloadable : reloadables) {
			reloadable.reload();
		}
	}

	public <T extends Reloadable> T registerReloadable(T reloadable) {
		this.reloadables.add(reloadable);
		return reloadable;
	}

	private @Nullable MetricsBase extractMetricsBase(Object metrics, Class<?> metricsClass) {
		try {
			Field baseField = metricsClass.getDeclaredField("metricsBase");
			baseField.setAccessible(true);
			return (MetricsBase) baseField.get(metrics);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public <T> void registerCustomCharts(final @NonNull T platformMetrics,
										 final @NonNull Class<T> platformMetricsClass) {
		final MetricsBase metrics = extractMetricsBase(platformMetrics, platformMetricsClass);

		if (metrics == null) {
			this.logger.warning("Failed to extract MetricsBase, not adding custom charts.");
			return;
		}

		final ConfigurationNode config = this.config().main();

		metrics.addCustomChart(new SimplePie("api_working", () ->
				this.apiProvider().isApiWorkingMetric()));

		metrics.addCustomChart(new SimplePie("server_data_sender_enabled", () ->
				config.node("server-data-sender", "enabled").getBoolean()
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("upload_placeholders_enabled", () ->
				config.node("server-data-sender", "placeholders", "enabled").getBoolean()
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("language", () ->
				this.language().getActiveLanguageCode()));

		metrics.addCustomChart(new SimplePie("auto_ban_on_website", () ->
				config.node("auto-ban-on-website").getBoolean()
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("not_registered_join_message", () ->
				config.node("not-registered-join-message").getBoolean()
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("user_sync_whitelist_enabled", () ->
				config.node("user-sync", "whitelist", "enabled").getBoolean()
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("user_sync_bans_enabled", () ->
				config.node("user-sync", "bans", "enabled").getBoolean()
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("announcements_enabled", () ->
				config.node("announcements", "enabled").getBoolean()
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("websend_command_executor_enabled", () ->
				config.node("websend", "command-executor", "enabled").getBoolean()
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("websend_console_capture_enabled", () ->
				config.node("websend", "console-capture", "enabled").getBoolean()
						? "Enabled" : "Disabled"));
	}

}
