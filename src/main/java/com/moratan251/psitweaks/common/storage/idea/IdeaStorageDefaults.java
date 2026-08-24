package com.moratan251.psitweaks.common.storage.idea;

/** イデアストレージ各カテゴリの既定容量。Fluid/Chemicalの内部量1000を1 Bとして扱う。 */
public final class IdeaStorageDefaults {
    public static final long RAW_UNITS_PER_BUCKET = 1_000L;
    public static final long FLUID_PER_TYPE_BUCKETS = 1_048_576L;
    public static final long CHEMICAL_PER_TYPE_BUCKETS = 33_554_432L;
    public static final long FLUID_PER_TYPE_RAW = FLUID_PER_TYPE_BUCKETS * RAW_UNITS_PER_BUCKET;
    public static final long CHEMICAL_PER_TYPE_RAW = CHEMICAL_PER_TYPE_BUCKETS * RAW_UNITS_PER_BUCKET;

    private IdeaStorageDefaults() {
    }
}
