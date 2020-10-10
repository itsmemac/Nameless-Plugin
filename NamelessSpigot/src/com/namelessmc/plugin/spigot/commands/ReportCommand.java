package com.namelessmc.plugin.spigot.commands;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.Message;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

import xyz.derkades.derkutils.ListUtils;

public class ReportCommand extends Command {

	public ReportCommand() {
		super(Config.COMMANDS.getConfig().getString("report"),
				Message.COMMAND_REPORT_DESCRIPTION.getMessage(),
				Message.COMMAND_REPORT_USAGE.getMessage("command", Config.COMMANDS.getConfig().getString("report")),
				Permission.COMMAND_REPORT);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		if (args.length < 2) {
			return false;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
			return false;
		}
		
		final Player player = (Player) sender;
		
		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final String username = args[0];
				final String[] reasonWordsArray = ListUtils.removeFirstStringFromArray(args);
				final String reason = String.join(" ", reasonWordsArray); //Join with space in between words
				final Optional<NamelessUser> user = NamelessPlugin.getInstance().api.getUser(player.getUniqueId());
				if (!user.isPresent()) {
					sender.sendMessage(Message.PLAYER_SELF_NOTREGISTERED.getMessage());
					return;
				}
				user.get().createReport(username, reason);
				sender.sendMessage(Message.COMMAND_REPORT_OUTPUT_SUCCESS.getMessage());
			} catch (final NamelessException e) {
				sender.sendMessage(Message.COMMAND_REPORT_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
				return;
			}
		});
		return true;
	}

}