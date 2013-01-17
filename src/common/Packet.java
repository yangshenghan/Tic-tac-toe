package cn.common;

import java.net.Socket;

public class Packet {
    public static final char UNKNOWN        = 0x00;

    public static final char VERSION        = 0x01;

    public static final char BEAT           = 0x10;

    public static final char LIST           = 0x11;

    public static final char RETRIVE        = 0x12;

    public static final char INVITE         = 0x20;

    public static final char ACCEPT         = 0x21;

    public static final char REJECT         = 0x22;

    public static final char SURRENDER      = 0x23;

    public static final char MOVE           = 0x24;

    public static final char REFRESH        = 0x25;

    public static final char NOTIFY         = 0x26;

    public static final char CONTINUE       = 0x27;

    public static final char WINNER         = 0x30;

    public static final char LOSER          = 0x31;

    public static final char DRAW           = 0x32;

    public static final char CONNECT        = 0xA1;

    public static final char DISCONNECT     = 0xA2;

    public static final char LEAVE          = 0xA3;

    public static final char WAITING        = 0xA4;

    public static final char CONFIRM        = 0xA5;

    public static final char READY          = 0xA6;

    // public static final char MESSAGE        = 0xA7;
}
