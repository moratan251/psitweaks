package com.moratan251.psitweaks.api.value;

/**
 * Marker for addon values that are plain, self-contained spell values rather
 * than world-context snapshots.
 *
 * <p>Implement this for custom value objects that are safe to copy, serialize,
 * and restore without consulting the current world. A matching
 * {@link PlainValueType} registration is still required before the value can be
 * accepted by plain-value parameters or CAD memory.</p>
 */
public interface PlainValue {
}
