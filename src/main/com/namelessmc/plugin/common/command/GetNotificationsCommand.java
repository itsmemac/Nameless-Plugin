package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.Notification;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class GetNotificationsCommand extends CommonCommand {

	public GetNotificationsCommand(final @NotNull NamelessPlugin plugin) {
		super(plugin,
				"get-notifications",
				Term.COMMAND_NOTIFICATIONS_USAGE,
				Term.COMMAND_NOTIFICATIONS_DESCRIPTION,
				Permission.COMMAND_GET_NOTIFICATIONS);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if (args.length != 0) {
			sender.sendMessage(this.getUsage());
			return;
		}

		if (!sender.isPlayer()) {
			sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTAPLAYER));
			return;
		}

		getScheduler().runAsync(() -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (!optApi.isPresent()) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL));
				return;
			}
			final NamelessAPI api = optApi.get();

			try {
				final Optional<NamelessUser> optional = this.getApiProvider().userFromPlayer(api, sender.getUniqueId(),sender.getName());

				if (!optional.isPresent()) {
					sender.sendMessage(getLanguage().getComponent(Term.PLAYER_SELF_NOTREGISTERED));
					return;
				}

				final NamelessUser user = optional.get();

				final List<Notification> notifications = user.getNotifications();

				notifications.sort((n1, n2) -> n2.getType().ordinal() - n1.getType().ordinal());

				if (notifications.size() == 0) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_NO_NOTIFICATIONS));
					return;
				}

				getScheduler().runSync(() -> {
					notifications.forEach((notification) -> {
						sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_NOTIFICATION,
								"url", notification.getUrl(),
								"message", notification.getMessage()));
					});
				});
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL));
				getLogger().logException(e);
			}
		});
	}

}