package cn.server;

import cn.common.*;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

public class Server {
    private HashMap<Integer, Agent> agents;

    private static ServerSocket socket;

    private boolean running = false;

    public Server() throws IOException {
        Logger.message("/* ======================== [ Tic-Tac-Toe Server ] ======================== *\\");
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

        Logger.info("The server is starting ...");
        Logger.info("Version: " + Configure.VERSION + " (" + Configure.VERSIONDATE + ")");
        this.agents = new HashMap<Integer, Agent>();

        Logger.info("Establishing the connection ...");
        socket = new ServerSocket(Configure.PORT, Configure.BACKLOG, InetAddress.getByName(Configure.ADDRESS));

        Logger.info("Listening: " + Configure.ADDRESS + ":" + Configure.PORT);
        this.running = true;

        Logger.info("Start to wait for clients ...");
        this.startWaitClient();

        Logger.info("Start the maintainment procedure ...");
        this.startPeriodMaintainment();

        Logger.info("Now waiting for the command ...");
        this.startCommandInteration();

        Logger.info("The server has started ...");
        while ( this.running ) {
            Thread.yield();
        }

        Logger.info("The server is stopping ...");
        this.shutdown();

        Logger.info("The server has stopped ...");
    }

    public Agent startAgentConnection(Agent agent) {
        Thread thread = new Thread(agent);
        thread.setDaemon(true);
        thread.start();
        agent.connect();
        this.agents.put((Integer)agent.getAgentData().getAgentId(), agent);
        this.broadcastGameList();
        return agent;
    }

    public Agent stopAgentConnection(Agent agent) {
        agent.disconnect();
        return agent;
    }

    public void broadcastGameList() {
        try {
            for ( Agent agent : this.agents.values() ) {
                agent.sendGameList();
            }
        } catch ( IOException exception ) {
            exception.printStackTrace();
        }
    }

    public void maintainAgentList(List<Agent> agents) {
        for ( Agent agent : agents ) {
            this.agents.remove((Integer)agent.getAgentData().getAgentId());
        }
        if ( agents.size() > 0 ) this.broadcastGameList();
    }

    public void maintainment() {
        final ArrayList<Agent> agents = new ArrayList<Agent>();
        for ( Agent agent : this.agents.values() ) {
            if ( agent.disconnected() ) agents.add(this.stopAgentConnection(agent));
        }

        this.maintainAgentList(agents);
    }

    public void shutdown() {
        final ArrayList<Agent> agents = new ArrayList<Agent>();

        this.running = false;

        for ( Agent agent : this.agents.values() ) {
            agents.add(this.stopAgentConnection(agent));
        }

        this.maintainAgentList(agents);

        try {
            for ( int times = 0 ; times < 10 ; ++times ) {
                Logger.message("Wait for " + (10 - times) + " seconds to shutdown the server ...");
                Thread.sleep(1000);
            }
        } catch ( InterruptedException exception ) {
            Logger.error("The server shutdown unexcepted ...");
            exception.printStackTrace();
        }

        try {
            socket.close();
        } catch ( IOException exception ) {
            Logger.error("The server shutdown unexcepted ...");
            exception.printStackTrace();
        }
    }

    public boolean running(boolean value) {
        return this.running = value;
    }

    public boolean running() {
        return this.running;
    }

    public Agent getAgent(int id) {
        return this.agents.get(id);
    }

    public Collection<Agent> getAgentCollection() {
        return this.agents.values();
    }

    public void startWaitClient() {
        final Server server = this;
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    while ( server.running() ) {
                        Socket connection = socket.accept();
                        server.startAgentConnection(new Agent(connection, server));
                    }
                } catch ( IOException exception ) {
                    exception.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void startPeriodMaintainment() {
        final Server server = this;
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    while ( server.running() ) {
                        Thread.sleep(1000);
                        server.maintainment();
                    }
                } catch ( InterruptedException exception ) {
                    exception.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void startCommandInteration() {
        final Server server = this;
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

                try {
                    while ( server.running() ) {
                        Logger.raw("command> ");
                        if ( ! Command.execute(buffer.readLine(), server) ) server.running(false);
                    }
                } catch ( IOException exception ) {
                    exception.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
        } catch ( IOException exception ) {
            exception.printStackTrace();
        }
    }
}
