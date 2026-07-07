package com.moratan251.psitweaks.common.spells.wrapper;

import com.moratan251.psitweaks.api.PsitweaksListLike;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class StringListWrapper implements Iterable<String>, PsitweaksListLike {
    public static final StringListWrapper EMPTY = new StringListWrapper(List.of());

    private final List<String> list;

    private StringListWrapper(List<String> list) {
        this.list = List.copyOf(list);
    }

    public static StringListWrapper make(List<String> values) {
        if (values == null || values.isEmpty()) {
            return EMPTY;
        }

        List<String> result = new ArrayList<>(values.size());
        for (String value : values) {
            if (value != null) {
                result.add(StringSpellHelper.sanitize(value));
            }
        }

        return result.isEmpty() ? EMPTY : new StringListWrapper(result);
    }

    public static StringListWrapper withAdded(StringListWrapper source, String value) {
        List<String> result = new ArrayList<>(safe(source).list);
        result.add(StringSpellHelper.sanitize(value));
        return make(result);
    }

    public static StringListWrapper withRemoved(StringListWrapper source, String value) {
        List<String> result = new ArrayList<>(safe(source).list);
        result.remove(StringSpellHelper.sanitize(value));
        return make(result);
    }

    public int size() {
        return list.size();
    }

    public String get(int index) {
        return list.get(index);
    }

    public List<String> asList() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public Iterator<String> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    private static StringListWrapper safe(StringListWrapper source) {
        return source == null ? EMPTY : source;
    }
}
