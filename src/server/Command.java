package cn.server;

import cn.common.*;
import java.util.ArrayList;

public class Command {
    public static boolean execute(String command, Server server) {
        String[] arguments = command.split(" ");
        switch ( arguments[0].toLowerCase() ) {
            case "" :
                break;
            case "shutdown" :
                return false;
            case "list" :
                {
                    if ( arguments.length == 1 || arguments[1].equals("players") ) {
                        int online = 0;
                        Logger.message("==== [ Online List Start ] ====");
                        for ( Agent agent : server.getAgentCollection() ) {
                            if ( ! agent.disconnected() ) {
                                online++;
                                Logger.message("\tPlayer #" + online + ": " + agent.getAgentData().getPlayerName() + " (" + agent.getAgentData().getAgentId() + ")");
                            }
                        }
                        Logger.message(" * There are " + online + " online players.");
                        Logger.message("==== [  Online List End  ] ====");
                    }
                    break;
                }
            case "kick" :
                {
                    Logger.message("==== [ Tic-Tac-Toe Server Kick Player Start ] ====");
                    final ArrayList<Agent> agents = new ArrayList<Agent>();
                    final Agent agent = server.getAgent(Integer.parseInt(arguments[1]));
                    if ( agent != null ) {
                        agents.add(server.stopAgentConnection(agent));
                        server.maintainAgentList(agents);
                    }
                    Logger.message("==== [  Tic-Tac-Toe Server Kick Player End  ] ====");
                    break;
                }
            case "maintain" :
                {
                    Logger.message("==== [ Tic-Tac-Toe Server Maintainment Start ] ====");
                    server.maintainment();
                    Logger.message("==== [  Tic-Tac-Toe Server Maintainment End  ] ====");
                    break;
                }
            case "help" :
                {
                    Logger.message("==== [ Tic-Tac-Toe Server Help Start ] ====");
                    Logger.message(" * shutdown");
                    Logger.message("    Shutdown the server and kick all players.");
                    Logger.message(" * list");
                    Logger.message("    List all the online players.");
                    Logger.message(" * kick player-id");
                    Logger.message("    Kick specified player with id.");
                    Logger.message(" * maintain");
                    Logger.message("    Maintain dead connection immediately.");
                    Logger.message(" * help");
                    Logger.message("    This help messages.");
                    Logger.message("==== [  Tic-Tac-Toe Server Help End  ] ====");
                    break;
                }
            default :
                Logger.message("Unknown command: " + arguments[0]);
        }
        return true;
    }
}
