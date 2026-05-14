package io.papermc.paper.command.brigadier.bukkit;

import com.google.common.collect.Iterators;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
// import io.papermc.paper.command.brigadier.PaperBrigadier;
// import io.papermc.paper.command.brigadier.PaperCommands;
// import io.papermc.paper.command.brigadier.bukkit.BukkitCommandNode;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitBrigForwardingMap extends HashMap<String, Command> {
	
	/*
    public static BukkitBrigForwardingMap INSTANCE = new BukkitBrigForwardingMap();
    private final EntrySet entrySet = new EntrySet();
    private final KeySet keySet = new KeySet();
    private final Values values = new Values();

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return PaperCommands.INSTANCE.getDispatcherInternal();
    }

    @Override
    public int size() {
        return this.getDispatcher().getRoot().getChildren().size();
    }

    @Override
    public boolean isEmpty() {
        return this.size() != 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        String stringKey = (String)key;
        return this.getDispatcher().getRoot().getChild(stringKey) != null;
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        if (!(value instanceof Command)) {
            return false;
        }
        for (CommandNode child : this.getDispatcher().getRoot().getChildren()) {
            if (!(child instanceof BukkitCommandNode)) continue;
            BukkitCommandNode bukkitCommandNode = (BukkitCommandNode)((Object)child);
            return bukkitCommandNode.getBukkitCommand().equals(value);
        }
        return false;
    }

    @Override
    public Command get(Object key) {
        CommandNode node = this.getDispatcher().getRoot().getChild((String)key);
        if (node == null) {
            return null;
        }
        if (node instanceof BukkitCommandNode) {
            BukkitCommandNode bukkitCommandNode = (BukkitCommandNode)((Object)node);
            return bukkitCommandNode.getBukkitCommand();
        }
        return PaperBrigadier.wrapNode(node);
    }

    @Override
    @Nullable
    public Command put(String key, Command value) {
        Command old = this.get(key);
        this.getDispatcher().getRoot().removeCommand(key);
        this.getDispatcher().getRoot().addChild((CommandNode)((Object)BukkitCommandNode.of(key, value)));
        return old;
    }

    @Override
    public Command remove(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        String string = (String)key;
        Command old = this.get(key);
        if (old != null) {
            this.getDispatcher().getRoot().removeCommand(string);
        }
        return old;
    }

    @Override
    public boolean remove(Object key, Object value) {
        Command old = this.get(key);
        if (Objects.equals(old, value)) {
            this.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends Command> m) {
        for (Map.Entry<? extends String, ? extends Command> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.getDispatcher().getRoot().clearAll();
    }

    @Override
    @NotNull
    public Set<String> keySet() {
        return this.keySet;
    }

    @Override
    @NotNull
    public Collection<Command> values() {
        return this.values;
    }

    @Override
    @NotNull
    public Set<Map.Entry<String, Command>> entrySet() {
        return this.entrySet;
    }

    private Map.Entry<String, Command> nodeToEntry(CommandNode<?> node) {
        if (node instanceof BukkitCommandNode) {
            BukkitCommandNode bukkitCommandNode = (BukkitCommandNode)((Object)node);
            return this.mutableEntry(bukkitCommandNode.getName(), bukkitCommandNode.getBukkitCommand());
        }
        Command wrapped = PaperBrigadier.wrapNode(node);
        return this.mutableEntry(node.getName(), wrapped);
    }

    private Map.Entry<String, Command> mutableEntry(final String key, final Command command) {
        return new Map.Entry<String, Command>(){

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Command getValue() {
                return command;
            }

            @Override
            public Command setValue(Command value) {
                return BukkitBrigForwardingMap.this.put(key, value);
            }
        };
    }

    final class EntrySet
    extends AbstractSet<Map.Entry<String, Command>> {
        EntrySet() {
        }

        @Override
        public int size() {
            return BukkitBrigForwardingMap.this.size();
        }

        @Override
        public void clear() {
            BukkitBrigForwardingMap.this.clear();
        }

        @Override
        public Iterator<Map.Entry<String, Command>> iterator() {
            return this.entryStream().iterator();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry)o;
            Object key = entry.getKey();
            Command candidate = BukkitBrigForwardingMap.this.get(key);
            return candidate != null && candidate.equals(entry.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry e2 = (Map.Entry)o;
                Object key = e2.getKey();
                Object value = e2.getValue();
                return BukkitBrigForwardingMap.this.remove(key, value);
            }
            return false;
        }

        @Override
        public Spliterator<Map.Entry<String, Command>> spliterator() {
            return this.entryStream().spliterator();
        }

        @Override
        public void forEach(Consumer<? super Map.Entry<String, Command>> action) {
            this.entryStream().forEach(action);
        }

        private Stream<Map.Entry<String, Command>> entryStream() {
            return BukkitBrigForwardingMap.this.getDispatcher().getRoot().getChildren().stream().map(BukkitBrigForwardingMap.this::nodeToEntry);
        }
    }

    final class KeySet
    extends AbstractSet<String> {
        KeySet() {
        }

        @Override
        public int size() {
            return BukkitBrigForwardingMap.this.size();
        }

        @Override
        public void clear() {
            BukkitBrigForwardingMap.this.clear();
        }

        @Override
        public Iterator<String> iterator() {
            return Iterators.transform(BukkitBrigForwardingMap.this.values.iterator(), Command::getName);
        }

        @Override
        public boolean contains(Object o) {
            return BukkitBrigForwardingMap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return BukkitBrigForwardingMap.this.remove(o) != null;
        }

        @Override
        public Spliterator<String> spliterator() {
            return this.entryStream().spliterator();
        }

        @Override
        public void forEach(Consumer<? super String> action) {
            this.entryStream().forEach(action);
        }

        private Stream<String> entryStream() {
            return BukkitBrigForwardingMap.this.getDispatcher().getRoot().getChildren().stream().map(CommandNode::getName);
        }
    }

    final class Values
    extends AbstractCollection<Command> {
        Values() {
        }

        @Override
        public Iterator<Command> iterator() {
            final Iterator iterator = new ArrayList(BukkitBrigForwardingMap.this.getDispatcher().getRoot().getChildren()).iterator();
            return new Iterator<Command>(){
                private CommandNode<CommandSourceStack> lastFetched;

                @Override
                public void remove() {
                    if (this.lastFetched == null) {
                        throw new IllegalStateException("next not yet called");
                    }
                    BukkitBrigForwardingMap.this.remove(this.lastFetched.getName());
                    iterator.remove();
                }

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Command next() {
                    CommandNode next;
                    this.lastFetched = next = (CommandNode)iterator.next();
                    if (next instanceof BukkitCommandNode) {
                        BukkitCommandNode bukkitCommandNode = (BukkitCommandNode)((Object)next);
                        return bukkitCommandNode.getBukkitCommand();
                    }
                    return PaperBrigadier.wrapNode(next);
                }
            };
        }

        @Override
        public int size() {
            return BukkitBrigForwardingMap.this.getDispatcher().getRoot().getChildren().size();
        }

        @Override
        public void clear() {
            BukkitBrigForwardingMap.this.clear();
        }
    }
    */
    
}
