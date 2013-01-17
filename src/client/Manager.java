package cn.client;

import cn.common.*;
import java.util.Stack;

public class Manager {
    private Client client;

    private Stack<Screen> screens;

    private Stack<Screen> suspendeds;

    public Manager(Client client) {
        this.client = client;
        this.screens = new Stack<Screen>();
        this.suspendeds = new Stack<Screen>();
    }

    public synchronized boolean startNewScreen(Screen screen) {
        if ( ! this.screens.empty() ) this.screens.peek().delete();
        screen.create();
        this.screens.push(screen);
        return true;
    }

    public synchronized boolean stopCurrentScreen() {
        if ( ! this.screens.empty() ) {
            Screen screen = this.screens.pop();
            screen.delete();
            return true;
        }
        return false;
    }

    public synchronized boolean suspendCurrentScreen() {
        if ( this.screens.empty() ) return false;
        this.suspendeds.push(this.screens.pop());
        return true;
    }

    public synchronized boolean resumePreviousScreen() {
        if ( this.suspendeds.empty() ) return false;
        this.screens.push(this.suspendeds.pop());
        return true;
    }

    public void update() {
        if ( ! this.screens.empty() ) this.screens.peek().update();
    }

    public void render() {
        if ( ! this.screens.empty() ) this.screens.peek().render();
    }

    public boolean updated(boolean value) {
        if ( this.screens.empty() ) return false;
        return this.screens.peek().updated(value);
    }

    public boolean updated() {
        if ( this.screens.empty() ) return false;
        return this.screens.peek().updated();
    }

    public Client getClient() {
        return this.client;
    }
}
