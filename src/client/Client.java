package cn.client;

import cn.common.*;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ConnectException;

public class Client {
    private Agent agent;

    private Manager manager;

    private Interface interfaces;

    private boolean running = false;

    public Client() {
        Logger.message("/* ======================== [ Tic-Tac-Toe Client ] ======================== *\\");
        Logger.message("|*   This program is free software; you can redistribute it and/or modify   *|");
        Logger.message("|*   it under the terms of the GNU General Public License as published by   *|");
        Logger.message("|*   the Free Software Foundation; either version 3, or (at your option)    *|");
        Logger.message("|*   any later version.                                                     *|");
        Logger.message("|*   This program is distributed in the hope that it will be useful,        *|");
        Logger.message("|*   but WITHOUT ANY WARRANTY; without even the implied warranty of         *|");
        Logger.message("|*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the           *|");
        Logger.message("|*   GNU General Public License for more details.                           *|");
        Logger.message("|*   You should have received a copy of the GNU General Public License      *|");
        Logger.message("|*   along with this program; if not, write to the Free Software            *|");
        Logger.message("|*   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA              *|");
        Logger.message("|*   02111-1307, USA.                                                       *|");
        Logger.message("|*   http://www.gnu.org/copyleft/gpl.html                                   *|");
        Logger.message("|*                                                                          *|");
        Logger.message("|*   This program is written by Yang Sheng-Han <shenghan.yang@gmail.com>    *|");
        Logger.message("\\* ======================================================================== */");

        Logger.info("The client is starting ...");
        Logger.info("Version: " + Configure.VERSION + " (" + Configure.VERSIONDATE + ")");
        this.running = true;

        this.manager = new Manager(this);
        this.interfaces = new Interface(this, this.manager);

        this.manager.startNewScreen(new ScreenTitle(this));
        Logger.info("The client has started ...");

        while ( this.running ) {
            this.manager.update();
        }

        Logger.info("The client is stopping ...");
        if ( this.agent != null ) this.agent.leave();

        Logger.info("The client has stopped ...");
    }

    public boolean running(boolean value) {
        return this.running = value;
    }

    public boolean running() {
        return this.running;
    }

    public Agent getAgent() {
        if ( this.agent == null ) {
            try {
                Socket socket = new Socket(InetAddress.getByName(Configure.ADDRESS), Configure.PORT);
                Agent agent = new Agent(socket, this);
                Thread thread = new Thread(agent);
                thread.setDaemon(true);
                thread.start();
                this.agent = agent;
            } catch ( ConnectException exception ) {
                exception.printStackTrace();
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
        }
        return this.agent;
    }

    public Manager getManager() {
        return this.manager;
    }

    public Interface getInterface() {
        return this.interfaces;
    }

    public static void main(String[] args) {
        Client client = new Client();
        System.exit(0);
    }
}
