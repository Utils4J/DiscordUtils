package de.mineking.discordutils.list;

import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.Manager;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.button.label.TextLabel;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.state.DataState;
import de.mineking.discordutils.ui.state.MessageSendState;
import de.mineking.discordutils.ui.state.SendState;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ListManager<C extends ICommandContext> extends Manager {
	private final UIManager uiManager;
	private final CommandManager<C, ?> commandManager;

	private OptionData pageOption = new OptionData(OptionType.INTEGER, "page", "page").setMinValue(1);

	public ListManager(@NotNull DiscordUtils.Builder<?> manager) {
		Checks.notNull(manager, "manager");

		uiManager = manager.getUIManager();
		commandManager = manager.getCommandManager();
	}

	/**
	 * @param option The {@link OptionData} for the page option
	 * @return {@code this}
	 */
	@NotNull
	public ListManager<C> setPageOption(@NotNull OptionData option) {
		Checks.notNull(option, "option");
		this.pageOption = option;
		return this;
	}

	/**
	 * @param path                 The command path
	 * @param object               A function to provide the {@link Listable} for the current {@link DataState}
	 * @param additionalComponents Additional {@link ComponentRow}s to add to the menu
	 * @return The resulting menu
	 */
	@NotNull
	@SuppressWarnings("unchecked")
	public <T extends ListEntry> MessageMenu createListMenu(@NotNull String path, @NotNull Function<DataState<MessageMenu>, Listable<T>> object, @NotNull ComponentRow... additionalComponents) {
		Checks.notNull(path, "path");
		Checks.notNull(object, "object");
		Checks.notNull(additionalComponents, "additionalComponents");


		var components = new ArrayList<ComponentRow>();

		components.add(ComponentRow.of(new ButtonComponent("first", ButtonColor.GRAY, Emoji.fromUnicode("⏪")).appendHandler(s -> {
			s.setState("page", 1);
			s.update();
		}).asDisabled(s -> s.getState("page", int.class) == 1), new ButtonComponent("back", ButtonColor.GRAY, Emoji.fromUnicode("⬅")).appendHandler(s -> {
			s.setState("page", int.class, p -> p - 1);
			s.update();
		}).asDisabled(s -> s.getState("page", int.class) == 1), new ButtonComponent("page", ButtonColor.GRAY, (TextLabel) state -> "\uD83D\uDCD6 " + state.getState("page", int.class) + "/" + state.getCache("maxpage")).asDisabled(true), new ButtonComponent("next", ButtonColor.GRAY, Emoji.fromUnicode("➡")).appendHandler(s -> {
			s.setState("page", int.class, p -> p + 1);
			s.update();
		}).asDisabled(s -> s.getState("page", int.class) == s.getCache("maxpage")), new ButtonComponent("last", ButtonColor.GRAY, Emoji.fromUnicode("⏩")).appendHandler(s -> {
			s.setState("page", Integer.MAX_VALUE);
			s.update();
		}).asDisabled(s -> s.getState("page", int.class) == s.getCache("maxpage"))));
		components.addAll(Arrays.asList(additionalComponents));

		return uiManager.createMenu("list." + path, (state, rows) -> state.<Listable<T>>getCache("object").render(state.getCache("context")).buildMessage(state, rows), components).cache(s -> {
			var o = object.apply(s);
			s.setCache("object", o);

			var context = new ListContext<T>(this, s.getEvent(), new ArrayList<>());

			var entries = o.getEntries(s, context);
			var max = (entries.size() - 1) / o.entriesPerPage() + 1;
			s.setCache("maxpage", max);
			s.setCache("size", context.entries().size());

			s.setCache("context", context);

			setEntries(s);
		}).effect("page", (state, name, old, n) -> {
			if (old != null && (int) old == (int) n) return;
			setEntries((DataState<MessageMenu>) state);
		});
	}

	private <T extends ListEntry> void setEntries(@NotNull DataState<MessageMenu> state) {
		var context = state.<ListContext<T>>getCache("context");
		if(context == null) return;

		var o = state.<Listable<T>>getCache("object");

		int page = Math.max(Math.min(state.getState("page", int.class), state.getCache("maxpage")), 1);
		state.setState("page", page);

		var entries = new ArrayList<>(o.getEntries(state, context));

		context.entries().clear();
		context.entries().addAll(entries.subList(((page - 1) * o.entriesPerPage()), Math.min((page * o.entriesPerPage()), entries.size())));
	}

	/**
	 * @param state                A consumer to configure the initial {@link SendState}
	 * @param object               A function to provide the {@link Listable} for the current {@link DataState}
	 * @param additionalComponents Additional {@link ComponentRow}s to add to the menu
	 * @return The resulting {@link ListCommand}
	 */
	@NotNull
	public <T extends ListEntry> ListCommand<C> createCommand(@NotNull BiConsumer<C, MessageSendState> state, @NotNull Function<DataState<MessageMenu>, Listable<T>> object, @NotNull ComponentRow... additionalComponents) {
		Checks.notNull(state, "state");
		Checks.notNull(object, "object");
		Checks.notNull(additionalComponents, "additionalComponents");

		var menu = new AtomicReference<MessageMenu>();

		return new ListCommand<>(menu, state, commandManager, pageOption) {
			public void register() {
				super.register();
				menu.set(createListMenu(getPath("."), object, additionalComponents));
			}
		};
	}

	/**
	 * @param object               A function to provide the {@link Listable} for the current {@link DataState}
	 * @param additionalComponents Additional {@link ComponentRow}s to add to the menu
	 * @return The resulting {@link ListCommand}
	 */
	@NotNull
	public <T extends ListEntry> ListCommand<C> createCommand(@NotNull Function<DataState<MessageMenu>, Listable<T>> object, @NotNull ComponentRow... additionalComponents) {
		return createCommand((c, state) -> {
		}, object, additionalComponents);
	}
}
