package de.mineking.discordutils.ui.components.button;

import de.mineking.discordutils.events.IEventHandler;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.components.button.label.EmojiLabel;
import de.mineking.discordutils.ui.components.button.label.LabelProvider;
import de.mineking.discordutils.ui.components.button.label.TextLabel;
import de.mineking.discordutils.ui.components.types.Component;
import de.mineking.discordutils.ui.state.DataState;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public class LinkComponent extends Component<ButtonInteractionEvent> {
	private final Function<DataState<MessageMenu>, String> url;
	private final LabelProvider label;
	private Predicate<DataState<MessageMenu>> disabled = state -> false;

	/**
	 * @param url   The url
	 * @param label The label
	 */
	public LinkComponent(@NotNull Function<DataState<MessageMenu>, String> url, @NotNull LabelProvider label) {
		super(UUID.randomUUID().toString());

		Checks.notNull(url, "url");
		Checks.notNull(label, "label");

		this.url = url;
		this.label = label;
	}

	@Override
	public int requiredSpace() {
		return 1;
	}

	/**
	 * @param url   The url
	 * @param label The label
	 */
	public LinkComponent(@NotNull Function<DataState<MessageMenu>, String> url, @NotNull String label) {
		this(url, (TextLabel) state -> label);
	}

	/**
	 * @param url   The url
	 * @param label The label
	 */
	public LinkComponent(@NotNull Function<DataState<MessageMenu>, String> url, @NotNull Emoji label) {
		this(url, (EmojiLabel) state -> label);
	}

	/**
	 * @param url   The url
	 * @param label The label
	 */
	public LinkComponent(@NotNull String url, @NotNull LabelProvider label) {
		this(state -> url, label);
	}

	/**
	 * @param url   The url
	 * @param label The label
	 */
	public LinkComponent(@NotNull String url, @NotNull String label) {
		this(url, (TextLabel) state -> label);
	}

	/**
	 * @param url   The url
	 * @param label The label
	 */
	public LinkComponent(@NotNull String url, @NotNull Emoji label) {
		this(url, (EmojiLabel) state -> label);
	}

	/**
	 * @param disabled A function to specify whether the component should be disabled based on the current {@link DataState}
	 * @return {@code this}
	 */
	@NotNull
	public LinkComponent asDisabled(@NotNull Predicate<DataState<MessageMenu>> disabled) {
		Checks.notNull(disabled, "disabled");
		this.disabled = disabled;
		return this;
	}

	/**
	 * @param disabled Whether this component should be disabled
	 * @return {@code this}
	 */
	@NotNull
	public LinkComponent asDisabled(boolean disabled) {
		return asDisabled(state -> disabled);
	}

	@Nullable
	@Override
	public IEventHandler<ButtonInteractionEvent> createHandler(@NotNull MessageMenu menu, @NotNull Predicate<ButtonInteractionEvent> filter) {
		return null;
	}

	@NotNull
	@Override
	public ActionComponent build(@NotNull String id, @NotNull DataState<MessageMenu> state) {
		var text = label.getText(state);
		var emoji = label.getEmoji(state);
		var url = this.url.apply(state);

		var button = emoji != null ? Button.link(url, emoji) : Button.link(url, Objects.requireNonNullElse(text, getName()));

		return button.withDisabled(disabled.test(state));
	}
}
