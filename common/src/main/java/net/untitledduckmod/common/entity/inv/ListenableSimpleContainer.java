package net.untitledduckmod.common.entity.inv;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

import java.util.ArrayList;
import java.util.List;

public class ListenableSimpleContainer extends SimpleContainer {
    private final List<Listener> listeners = new ArrayList<>();

    public ListenableSimpleContainer(int size) {
        super(size);
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        for (Listener listener : this.listeners) {
            listener.containerChanged(this);
        }
    }

    public interface Listener {
        void containerChanged(Container container);
    }
}
