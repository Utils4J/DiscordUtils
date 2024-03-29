package de.mineking.discordutils.commands.condition;

import de.mineking.discordutils.commands.CommandManager;
import net.dv8tion.jda.api.entities.Guild;

public enum Scope {
	/**
	 * Registered per guild. You are able to control on which guild this command is registered
	 *
	 * @see IRegistrationCondition#shouldRegister(CommandManager, Guild, de.mineking.discordutils.commands.Cache)
	 */
	GUILD,
	/**
	 * Registered globally but only available on guilds
	 */
	GUILD_GLOBAL,
	/**
	 * Registered globally. Commands with this scope can be executed on all guilds and in direct messages with the bot
	 */
	GLOBAL
}
