package list;

import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.commands.CommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import shared.AutocompleteContext;
import shared.CommandContext;

public class ListBot {
	public static void main(String[] args) {
		new ListBot(args[0]);
	}

	public final JDA jda;
	public final DiscordUtils<ListBot> discordUtils;

	public ListBot(String token) {
		jda = JDABuilder.createDefault(token)
				.setStatus(OnlineStatus.ONLINE)
				.build();

		discordUtils = DiscordUtils.create(jda, this)
				.useEventManager(null)
				.useUIManager(null)
				.useCommandManager(
						CommandContext::new,
						AutocompleteContext::new,
						CommandManager::updateCommands
				)
				.useListManager(config -> config.getManager().getCommandManager().registerCommand(config.createCommand(state -> new TestList()).withName("list_test")))
				.build();
	}
}