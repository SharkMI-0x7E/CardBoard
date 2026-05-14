package org.bukkit.craftbukkit.scoreboard;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.RenderType;
import org.jetbrains.annotations.NotNull;

public class CardboardCriteria implements Criteria {

    private static final Map<String, CardboardCriteria> DEFAULTS;
    private static final CardboardCriteria DUMMY;

    static {
        ImmutableMap.Builder<String, CardboardCriteria> defaults = ImmutableMap.builder();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) ObjectiveCriteria.CRITERIA_CACHE).entrySet())
            defaults.put(entry.getKey().toString(), new CardboardCriteria((ObjectiveCriteria) entry.getValue()));
        DEFAULTS = defaults.build();
        DUMMY = DEFAULTS.get("dummy");
    }

    final ObjectiveCriteria criteria;
    final String bukkitName;

    private CardboardCriteria(String bukkitName) {
        this.bukkitName = bukkitName;
        this.criteria = DUMMY.criteria;
    }

    private CardboardCriteria(ObjectiveCriteria criteria) {
        this.criteria = criteria;
        this.bukkitName = criteria.getName();
    }

    static CardboardCriteria getFromNMS(Objective objective) {
        return DEFAULTS.get(objective.getCriteria().getName());
    }

    public static CardboardCriteria getFromBukkit(String name) {
        final CardboardCriteria criteria = DEFAULTS.get(name);
        return (criteria != null) ? criteria : new CardboardCriteria(name);
    }

    @Override
    public boolean equals(Object that) {
        return (!(that instanceof CardboardCriteria)) ? false : ((CardboardCriteria) that).bukkitName.equals(this.bukkitName);
    }

    @Override
    public int hashCode() {
        return this.bukkitName.hashCode() ^ CardboardCriteria.class.hashCode();
    }

    @Override
    public String getName() {
        return bukkitName;
    }

    @Override
    public boolean isReadOnly() {
        return criteria.isReadOnly();
    }

    @Override
    public RenderType getDefaultRenderType() {
        return RenderType.values()[criteria.getDefaultRenderType().ordinal()];
    }

}