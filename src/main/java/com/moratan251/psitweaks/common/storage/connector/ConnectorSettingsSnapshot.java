package com.moratan251.psitweaks.common.storage.connector;

import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Connector settings without the owner, export cooldowns or any stored resources.
 * A null section means "leave this part of the connector unchanged" when applied.
 */
public record ConnectorSettingsSnapshot(@Nullable List<Face> faces, @Nullable List<ConnectorExportSettings> export,
                                        @Nullable List<Slot> slots, @Nullable Input input) {
    public static final int FACES = 6;

    public ConnectorSettingsSnapshot {
        faces = faces == null ? null : sized(faces, FACES);
        export = export == null ? null : sized(export, ConnectorExportSettings.TYPES);
        slots = slots == null ? null : sized(slots, IdeaspaceConnectorBlockEntity.SLOTS);
    }

    public boolean isEmpty() {
        return faces == null && export == null && slots == null && input == null;
    }

    /** One face of the shared profile or of an individual slot profile, in {@code Direction} order. */
    public record Face(ConnectorSideMode mode, boolean automatic, ConnectorRedstoneMode redstone) {
        public static final Face DEFAULT = new Face(ConnectorSideMode.BOTH, false, ConnectorRedstoneMode.ALWAYS);
    }

    /** Individual settings of one published slot. */
    public record Profile(List<Face> faces, List<ConnectorExportSettings> export) {
        public Profile {
            faces = sized(faces, FACES);
            export = sized(export, ConnectorExportSettings.TYPES);
        }

        public static Profile defaults() {
            return new Profile(defaultFaces(), defaultExport());
        }
    }

    /** A published resource and, when present, the slot's individual settings. */
    public record Slot(ConnectorResource resource, @Nullable Profile individual) {
        public static final Slot EMPTY = new Slot(ConnectorResource.EMPTY, null);
    }

    public record Input(ConnectorInputMode mode, List<ConnectorResource> filters) {
        public Input {
            filters = sized(filters, IdeaspaceConnectorBlockEntity.SLOTS);
        }
    }

    public static List<Face> defaultFaces() {
        return Collections.nCopies(FACES, Face.DEFAULT);
    }

    public static List<ConnectorExportSettings> defaultExport() {
        List<ConnectorExportSettings> values = new ArrayList<>(ConnectorExportSettings.TYPES);
        for (int type = 0; type < ConnectorExportSettings.TYPES; type++) values.add(ConnectorExportSettings.defaults(type));
        return List.copyOf(values);
    }

    private static <T> List<T> sized(List<T> values, int size) {
        if (values.size() != size) throw new IllegalArgumentException("Expected " + size + " entries, got " + values.size());
        return List.copyOf(values);
    }
}
