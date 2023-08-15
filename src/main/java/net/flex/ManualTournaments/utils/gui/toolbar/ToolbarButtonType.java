package net.flex.ManualTournaments.utils.gui.toolbar;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ToolbarButtonType {
    PREV_BUTTON,
    CURRENT_BUTTON,
    NEXT_BUTTON,
    UNASSIGNED;

    private static final Map<Integer, ToolbarButtonType> DEFAULT_MAPPINGS = Stream.of(
        new AbstractMap.SimpleImmutableEntry<>(3, PREV_BUTTON),
        new AbstractMap.SimpleImmutableEntry<>(4, CURRENT_BUTTON),
        new AbstractMap.SimpleImmutableEntry<>(5, NEXT_BUTTON)
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public static ToolbarButtonType getDefaultForSlot(int slot) {
        return DEFAULT_MAPPINGS.getOrDefault(slot, ToolbarButtonType.UNASSIGNED);
    }
}
