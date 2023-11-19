package de.mineking.discordutils.commands;

import de.mineking.discordutils.commands.condition.registration.Scope;
import de.mineking.discordutils.commands.context.ContextBase;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface CommandFilter<C extends ContextBase<? extends GenericCommandInteractionEvent>> {
	/**
	 * @param name The required name for the command
	 * @return A {@link CommandFilter} that only accepts commands with the specified name
	 */
	static <C extends ContextBase<? extends GenericCommandInteractionEvent>> CommandFilter<C> name(String name) {
		return command -> command.name.equals(name);
	}

	/**
	 * @return A {@link CommandFilter} that only accepts commands without a parent
	 */
	static <C extends ContextBase<? extends GenericCommandInteractionEvent>> CommandFilter<C> top() {
		return command -> command.getParent() == null;
	}

	/**
	 * @param scope The required {@link Scope} for the command
	 * @return A {@link CommandFilter} that only accepts commands with the provided scope
	 */
	static <C extends ContextBase<? extends GenericCommandInteractionEvent>> CommandFilter<C> scope(Scope scope) {
		return command -> command.getRegistration().getScope() == scope;
	}

	static <C extends ContextBase<? extends GenericCommandInteractionEvent>> CommandFilter<C> of(Predicate<Command<C>> filter) {
		return filter::test;
	}

	/**
	 * @param command The {@link Command}
	 * @return Whether the command should be accepted
	 */
	boolean filter(@NotNull Command<C> command);

	/**
	 * @return A {@link CommandFilter} that only accepts commands that match both this and the other filter
	 */
	@NotNull
	default CommandFilter<C> and(@NotNull CommandFilter<C> other) {
		Checks.notNull(other, "other");
		return c -> filter(c) && other.filter(c);
	}

	/**
	 * @return A {@link CommandFilter} that accepts commands that match this or the other filter
	 */
	@NotNull
	default CommandFilter<C> or(@NotNull CommandFilter<C> other) {
		Checks.notNull(other, "other");
		return c -> filter(c) || other.filter(c);
	}

	/**
	 * @return A {@link CommandFilter} that only accepts commands that are not accepted by this filter
	 */
	@NotNull
	default CommandFilter<C> invert() {
		return c -> !filter(c);
	}

	/**
	 * @return A {@link CommandFilter} that accepts commands that are accepted by any of the provided filters
	 */
	@NotNull
	@SafeVarargs
	static <C extends ContextBase<? extends GenericCommandInteractionEvent>> CommandFilter<C> any(@NotNull CommandFilter<C> filter, @NotNull CommandFilter<C>... filters) {
		Checks.notNull(filter, "filter");
		Checks.notNull(filters, "filters");

		for(var f : filters) filter = filter.or(f);
		return filter;
	}

	/**
	 * @return A {@link CommandFilter} that only accepts commands that are accepted by all the provided filters
	 */
	@NotNull
	@SafeVarargs
	static <C extends ContextBase<? extends GenericCommandInteractionEvent>> CommandFilter<C> all(@NotNull CommandFilter<C> filter, @NotNull CommandFilter<C>... filters) {
		Checks.notNull(filter, "filter");
		Checks.notNull(filters, "filters");

		for(var f : filters) filter = filter.and(f);
		return filter;
	}
}