package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
import com.moratan251.psitweaks.api.value.ContextualValue;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.translation.DisplayNameTranslationRepository;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public final class DisplayNameTargetHelper {
    private static final String ENGLISH = "en_us";

    private DisplayNameTargetHelper() {
    }

    public static String getDisplayName(SpellContext context, Object target) throws SpellRuntimeException {
        if (target instanceof Entity entity) {
            context.verifyEntity(entity);
            return entityName(context, entity);
        }
        if (target instanceof SpellItemValue item) {
            if (item.isEmpty()) {
                return "";
            }
            return componentToString(context, item.copyStack().getHoverName());
        }
        if (target instanceof BlockValue block) {
            return componentToString(context, block.displayName());
        }
        if (target instanceof Vector3 vector) {
            return componentToString(context, BlockValueHelper.snapshotVector(context, vector).displayName());
        }
        if (target instanceof ContextualValue) {
            return "";
        }
        return "";
    }

    public static List<String> getDisplayNames(SpellContext context, Object source) throws SpellRuntimeException {
        if (source == null) {
            return List.of();
        }

        PsitweaksListAdapter<Object> adapter = PsitweaksListAdapters.findAdapter(source.getClass()).orElseThrow(
                () -> new SpellRuntimeException(SpellRuntimeException.NULL_TARGET)
        );
        List<String> result = new ArrayList<>();
        int size = adapter.size(source);
        for (int i = 0; i < size; i++) {
            result.add(getDisplayName(context, adapter.get(source, i)));
        }
        return List.copyOf(result);
    }

    private static String entityName(SpellContext context, Entity entity) {
        if (entity instanceof Player player) {
            return StringSpellHelper.sanitize(player.getGameProfile().getName());
        }
        if (entity.hasCustomName() && entity.getCustomName() != null) {
            return componentToString(context, entity.getCustomName());
        }
        return componentToString(context, entity.getType().getDescription());
    }

    private static String componentToString(SpellContext context, Component component) {
        if (component == null) {
            return "";
        }
        if (component.getContents() instanceof TranslatableContents translatable) {
            StringBuilder result = new StringBuilder(resolveTranslatable(context, translatable));
            for (Component sibling : component.getSiblings()) {
                result.append(componentToString(context, sibling));
            }
            return StringSpellHelper.sanitize(result.toString());
        }
        return StringSpellHelper.sanitize(component.getString());
    }

    private static String resolveTranslatable(SpellContext context, TranslatableContents contents) {
        String key = contents.getKey();
        Object[] args = contents.getArgs();
        Object[] resolvedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            resolvedArgs[i] = arg instanceof Component component ? componentToString(context, component) : arg;
        }

        String language = languageCode(context);
        String clientTranslation = clientTranslation(language, key, resolvedArgs);
        if (clientTranslation != null) {
            return clientTranslation;
        }

        String pattern = resolveKey(language, key);
        if (args.length == 0) {
            return pattern;
        }

        try {
            return String.format(Locale.ROOT, pattern, resolvedArgs);
        } catch (IllegalArgumentException ignored) {
            return pattern;
        }
    }

    private static String resolveKey(String language, String key) {
        if (key == null || key.isBlank()) {
            return "";
        }

        String namespace = namespaceFromTranslationKey(key);
        String translated = translation(namespace, language, key);
        if (translated != null) {
            return translated;
        }
        if (!ENGLISH.equals(language)) {
            translated = translation(namespace, ENGLISH, key);
            if (translated != null) {
                return translated;
            }
        }
        return key;
    }

    private static String clientTranslation(String language, String key, Object[] args) {
        if (FMLEnvironment.dist != Dist.CLIENT || key == null || key.isBlank()) {
            return null;
        }
        String selectedLanguage = clientSelectedLanguage();
        if (selectedLanguage == null || !selectedLanguage.equals(language)) {
            return null;
        }

        try {
            Class<?> i18nClass = Class.forName("net.minecraft.client.resources.language.I18n");
            Method exists = i18nClass.getMethod("exists", String.class);
            if (!Boolean.TRUE.equals(exists.invoke(null, key))) {
                return null;
            }
            Method get = i18nClass.getMethod("get", String.class, Object[].class);
            Object translated = get.invoke(null, key, (Object) args);
            return translated instanceof String text ? text : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static String clientSelectedLanguage() {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Method getInstance = minecraftClass.getMethod("getInstance");
            Object minecraft = getInstance.invoke(null);
            if (minecraft == null) {
                return null;
            }

            Method getLanguageManager = minecraftClass.getMethod("getLanguageManager");
            Object languageManager = getLanguageManager.invoke(minecraft);
            if (languageManager == null) {
                return null;
            }

            Method getSelected = languageManager.getClass().getMethod("getSelected");
            Object selected = getSelected.invoke(languageManager);
            return selected instanceof String language ? language.toLowerCase(Locale.ROOT) : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static String translation(String namespace, String language, String key) {
        return DisplayNameTranslationRepository.getTranslation(namespace, language, key);
    }

    private static String languageCode(SpellContext context) {
        if (context != null && context.caster instanceof ServerPlayer serverPlayer) {
            String language = serverPlayer.getLanguage();
            if (language != null && !language.isBlank()) {
                return language.toLowerCase(Locale.ROOT);
            }
        }
        return ENGLISH;
    }

    private static String namespaceFromTranslationKey(String key) {
        int firstSeparator = key.indexOf('.');
        if (firstSeparator <= 0) {
            return "minecraft";
        }

        String firstPart = key.substring(0, firstSeparator);
        int secondSeparator = key.indexOf('.', firstSeparator + 1);
        if (secondSeparator > firstSeparator && hasRegistryNamespace(firstPart)) {
            return key.substring(firstSeparator + 1, secondSeparator);
        }
        return firstPart;
    }

    private static boolean hasRegistryNamespace(String firstPart) {
        return switch (firstPart) {
            case "attribute", "block", "effect", "enchantment", "entity", "item", "painting", "stat" -> true;
            default -> false;
        };
    }

}