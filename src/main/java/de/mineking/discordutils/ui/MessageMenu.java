package de.mineking.discordutils.ui;

import com.google.gson.JsonParser;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.state.DataState;
import de.mineking.discordutils.ui.state.MessageSendState;
import de.mineking.discordutils.ui.state.State;
import de.mineking.discordutils.ui.state.UpdateState;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class MessageMenu extends Menu {
	private final MessageRenderer renderer;
	private final List<ComponentRow> components;

	private Consumer<DataState<MessageMenu>> cache;

	MessageMenu(@NotNull UIManager manager, @NotNull String id, @NotNull MessageRenderer renderer, @NotNull List<ComponentRow> components) {
		super(manager, id);

		this.renderer = renderer;
		this.components = components;
	}

	/**
	 * @return An unmodifiable copy of this menu's components
	 */
	@NotNull
	public List<ComponentRow> getComponents() {
		return Collections.unmodifiableList(components);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initialize(@NotNull DataState<?> state) {
		if(cache != null) cache.accept((DataState<MessageMenu>) state);
	}

	/**
	 * @param state The {@link UpdateState}
	 * @return The rendered message for the provided state
	 */
	@NotNull
	public MessageEditData buildMessage(@NotNull UpdateState state) {
		Checks.notNull(state, "state");

		var data = new StringBuilder(state.getData().toString());

		var message = renderer.buildMessage(state, this.components.stream().map(r -> ActionRow.of(r.getComponents().stream().map(c -> {
			var id = this.getId() + ":" + c.getName() + ":";

			if(!data.isEmpty()) {
				var pos = Math.min(Button.ID_MAX_LENGTH - id.length(), data.length());
				id += data.substring(0, pos);
				data.delete(0, pos);
			}

			return c.build(id, state);
		}).toList())).toList());

		if(!data.isEmpty())
			throw new IllegalStateException("State is too large. Either add more components to give more space or shrink your state size: [%d] %s, left: [%d] %s".formatted(state.getData().toString().length(), state.getData().toString(), data.length(), data.toString()));

		return message.build();
	}

	@NotNull
	@Override
	public MessageMenu effect(@NotNull EffectHandler<?> handler) {
		return (MessageMenu) super.effect(handler);
	}

	@NotNull
	@Override
	public <T> MessageMenu effect(@NotNull String name, @NotNull EffectHandler<T> handler) {
		return (MessageMenu) super.effect(name, handler);
	}


	/**
	 * @param handler A handler that is called before rendering. This can be used to initialize cache values
	 * @return {@code this}
	 */
	@NotNull
	public MessageMenu cache(@NotNull Consumer<DataState<MessageMenu>> handler) {
		this.cache = handler;
		return this;
	}

	@NotNull
	@Override
	public MessageSendState createState(@Nullable State<?> state) {
		return new MessageSendState(this, state == null ? JsonParser.parseString("{}").getAsJsonObject() : state.getData());
	}

	@NotNull
	@Override
	public MessageSendState createState() {
		return (MessageSendState) super.createState();
	}

	/**
	 * @param channel The channel to send the menu in
	 */
	public void display(@NotNull MessageChannel channel) {
		createState().display(channel);
	}

	/**
	 * @param event     An {@link IReplyCallback} to reply the menu to
	 * @param ephemeral Whether the message should only be visible to the user who created the interaction
	 */
	public void display(@NotNull IReplyCallback event, boolean ephemeral) {
		createState().display(event, ephemeral);
	}

	/**
	 * @param event An {@link IReplyCallback} to reply the menu to
	 */
	public void display(@NotNull IReplyCallback event) {
		display(event, true);
	}
}
