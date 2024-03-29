package de.mineking.discordutils.ui.modal;

import com.google.gson.JsonParser;
import de.mineking.discordutils.events.IEventHandler;
import de.mineking.discordutils.events.handlers.FilteredEventHandler;
import de.mineking.discordutils.ui.EffectHandler;
import de.mineking.discordutils.ui.Menu;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.state.DataState;
import de.mineking.discordutils.ui.state.State;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModalMenu extends Menu {
	private final List<TextComponent> components;
	private final Function<DataState<ModalMenu>, String> title;

	private final BiConsumer<DataState<ModalMenu>, ModalResponse> handler;

	private Consumer<DataState<ModalMenu>> cache;

	public ModalMenu(@NotNull UIManager manager, @NotNull String id, @NotNull Function<DataState<ModalMenu>, String> title, @NotNull List<TextComponent> components, @NotNull BiConsumer<DataState<ModalMenu>, ModalResponse> handler) {
		super(manager, id);
		this.title = title;
		this.components = components;
		this.handler = handler;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initialize(@NotNull DataState<?> state) {
		if(cache != null) cache.accept((DataState<ModalMenu>) state);
	}

	public IEventHandler<ModalInteractionEvent> createHandler() {
		return new FilteredEventHandler<>(ModalInteractionEvent.class, event -> event.getModalId().startsWith(getId() + ":")) {
			@Override
			public void handleEvent(ModalInteractionEvent event) {
				handler.accept(DataState.load(ModalMenu.this, event), new ModalResponse(event.getValues()));
			}
		};
	}

	@NotNull
	public Modal buildModal(@NotNull DataState<ModalMenu> state) {
		Checks.notNull(state, "state");

		var data = new StringBuilder(state.getData().toString());

		var id = this.getId() + ":";

		if(!data.isEmpty()) {
			var pos = Math.min(Modal.MAX_ID_LENGTH - id.length(), data.length());
			id += data.substring(0, pos);
			data.delete(0, pos);
		}

		var temp = Modal.create(id, title.apply(state)).addComponents(components.stream().map(c -> {
			var name = c.getName() + ":";

			if(!data.isEmpty()) {
				var pos = Math.min(TextInput.MAX_ID_LENGTH - name.length(), data.length());
				name += data.substring(0, pos);
				data.delete(0, pos);
			}

			return ActionRow.of(c.build(name, state));
		}).toList()).build();

		if(!data.isEmpty())
			throw new IllegalStateException("State is too large. Either add more components to give more space or shrink your state size: [%d] %s, left: [%d] %s".formatted(state.getData().toString().length(), state.getData().toString(), data.length(), data.toString()));

		return temp;
	}

	@NotNull
	@Override
	public ModalMenu effect(@NotNull EffectHandler<?> handler) {
		return (ModalMenu) super.effect(handler);
	}

	@NotNull
	@Override
	public <T> ModalMenu effect(@NotNull String name, @NotNull EffectHandler<T> handler) {
		return (ModalMenu) super.effect(name, handler);
	}

	/**
	 * @param handler A handler that is called before rendering. This can be used to initialize cache values
	 * @return {@code this}
	 */
	@NotNull
	public ModalMenu cache(@NotNull Consumer<DataState<ModalMenu>> handler) {
		this.cache = handler;
		return this;
	}

	@NotNull
	@Override
	public ModalSendState createState(@Nullable State<?> state) {
		return new ModalSendState(this, state == null ? JsonParser.parseString("{}").getAsJsonObject() : state.getData());
	}

	@NotNull
	@Override
	public ModalSendState createState() {
		return (ModalSendState) super.createState();
	}
}
