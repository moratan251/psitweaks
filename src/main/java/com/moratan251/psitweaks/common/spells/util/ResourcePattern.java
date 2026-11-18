package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaksqol.api.StringSpellHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

/**
 * A String pattern of the spell filters over resource IDs. The pattern uses the {@link WildcardStringMatcher} rules;
 * without a ':' only the path of an ID is compared, so it matches that path in every namespace. A pattern that starts
 * with # is compared with the tag IDs of the resource instead, under the same rules, and one matching tag is enough.
 * A resource without tags never matches a tag pattern, and # alone matches nothing. The answer for each tag ID is kept,
 * so a tag shared by many resources is compared once. Not thread-safe: use a pattern within one piece's execution.
 */
public final class ResourcePattern {
    private final WildcardStringMatcher matcher;
    private final boolean fullId;
    private final boolean tag;
    private final Map<ResourceLocation, Boolean> tagResults = new HashMap<>();

    private ResourcePattern(WildcardStringMatcher matcher, boolean fullId, boolean tag) {
        this.matcher = matcher;
        this.fullId = fullId;
        this.tag = tag;
    }

    public static ResourcePattern compile(@Nullable String pattern) {
        String sanitized = StringSpellHelper.sanitize(pattern);
        boolean tag = sanitized.startsWith("#");
        String body = tag ? sanitized.substring(1) : sanitized;
        return new ResourcePattern(WildcardStringMatcher.compile(body), body.indexOf(':') >= 0, tag);
    }

    /** Whether the pattern compares tags, i.e. starts with # once sanitized. */
    public static boolean isTagPattern(@Nullable String pattern) {
        return StringSpellHelper.sanitize(pattern).startsWith("#");
    }

    /**
     * A filter over the IDs of the registry: a resource matches by its ID, or by its tags for a # pattern. Null and an
     * empty pattern match everything. The answer for each ID is kept for the life of the filter.
     */
    public static Predicate<ResourceLocation> filter(@Nullable String pattern, Registry<?> registry) {
        if (pattern == null || pattern.isEmpty()) {
            return id -> true;
        }
        ResourcePattern compiled = compile(pattern);
        Map<ResourceLocation, Boolean> results = new HashMap<>();
        return id -> results.computeIfAbsent(id, key -> compiled.matches(registry, key));
    }

    public boolean isTag() {
        return tag;
    }

    /** Compares the resource with this ID in the registry: its ID, or its tag IDs for a # pattern. */
    public boolean matches(Registry<?> registry, ResourceLocation id) {
        if (!tag) {
            return matchesLocation(id);
        }
        return registry.getHolder(id)
                .map(holder -> holder.tags().anyMatch(key -> matchesTag(key.location())))
                .orElse(false);
    }

    /** Whether one of the tag IDs matches a # pattern. A pattern without # matches no tags. */
    public boolean matchesAnyTag(Iterable<ResourceLocation> tagIds) {
        if (!tag) {
            return false;
        }
        for (ResourceLocation tagId : tagIds) {
            if (tagId != null && matchesTag(tagId)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesTag(ResourceLocation tagId) {
        return tagResults.computeIfAbsent(tagId, this::matchesLocation);
    }

    private boolean matchesLocation(ResourceLocation id) {
        return matcher.matches(fullId ? id.toString() : id.getPath());
    }
}
