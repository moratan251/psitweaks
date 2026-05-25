package com.moratan251.psitweaks.api;

/**
 * Optional interface for spell value types that should be accepted as Psitweaks
 * list inputs without registering a dedicated adapter.
 */
public interface PsitweaksListLike {
    int size();
}
