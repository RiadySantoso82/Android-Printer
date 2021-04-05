package com.riady.printerlib.Command;

public class Command {
    private static final byte ESC = 0x1B;
    private static final byte FS = 0x1C;
    private static final byte GS = 0x1D;

    public static byte[] ESC_Init = new byte[] {ESC, '@' };
    public static byte[] ESC_Align = new byte[] {ESC, 'a', 0x00 };
    public static byte[] ESC_J = new byte[] {ESC, 'J', 0x00 };
    public static byte[] GS_ExclamationMark = new byte[] {GS, '!', 0x00 };
    public static byte[] ESC_t = new byte[] {ESC, 't', 0x00 };
    public static byte[] ESC_M = new byte[] {ESC, 'M', 0x00 };
    public static byte[] FS_and = new byte[] {FS, '&' };
    public static byte[] FS_dot = new byte[] {FS, 46 };
    public static byte[] GS_V_m_n = new byte[] {GS, 'V', 'B', 0x00 };
}
