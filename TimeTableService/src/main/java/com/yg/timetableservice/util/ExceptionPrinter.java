package com.yg.timetableservice.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * print exception stack
 */
public class ExceptionPrinter {
    public static String getStackInfo(Throwable e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw =  new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
        return sw.toString();
    }
}
