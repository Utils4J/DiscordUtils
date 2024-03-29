package de.mineking.discordutils.ui.components.types;

import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ComponentRow {
	/**
	 * @return The components of this {@link ComponentRow}
	 */
	@NotNull
	List<? extends Component<?>> getComponents();

	/**
	 * @return The total required space of all components in this row
	 */
	default int size() {
		return getComponents().stream().mapToInt(Component::requiredSpace).sum();
	}

	/**
	 * @param components The components to use
	 * @return A {@link ComponentRow} holding the provided components
	 */
	@NotNull
	static ComponentRow of(@NotNull List<? extends Component<?>> components) {
		Checks.notNull(components, "components");

		return () -> components;
	}

	/**
	 * @param components The components to use
	 * @return A {@link ComponentRow} holding the provided components
	 */
	@NotNull
	static ComponentRow of(@NotNull Component<?>... components) {
		return of(Arrays.asList(components));
	}

	/**
	 * @param components The components to use
	 * @return A list of {@link ComponentRow}s holding teh provided components
	 */
	@NotNull
	static List<ComponentRow> ofMany(@NotNull List<? extends Component<?>> components) {
		var result = new ArrayList<ComponentRow>();

		var temp = new ArrayList<Component<?>>();
		int tempSize = 0;

		for(var c : components) {
			if(5 - tempSize < c.requiredSpace()) {
				result.add(ComponentRow.of(temp));

				temp = new ArrayList<>();
				tempSize = 0;
			}

			temp.add(c);
			tempSize += c.requiredSpace();
		}

		if(!temp.isEmpty()) result.add(ComponentRow.of(temp));

		return result;
	}

	/**
	 * @param components The components to use
	 * @return A list of {@link ComponentRow}s holding teh provided components
	 */
	@NotNull
	static List<ComponentRow> ofMany(@NotNull Component<?>... components) {
		return ofMany(Arrays.asList(components));
	}
}
