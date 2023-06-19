
/*
# © Copyright 2019-2023, Clinacuity Inc. All Rights Reserved.
#
# This file is part of CliniDeID.
# CliniDeID is free software: you can redistribute it and/or modify it under the terms of the
# GNU General Public License as published by the Free Software Foundation,
# either version 3 of the License, or any later version.
# CliniDeID is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
# PURPOSE. See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with CliniDeID.
# If not, see <https://www.gnu.org/licenses/>.
# =========================================================================   
*/

import java.io.*;
import java.net.Socket;
import static java.nio.charset.StandardCharsets.UTF_8;

public class StopRnn {
    private static String hostName="localhost";
    private static int portNumber=4444;

    public static void main(String []argv) {
        try {
    	    Socket kkSocket = new Socket(hostName, portNumber);
	        PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
            String endMsg = "--<eosc>--";
            byte[] bText = endMsg.getBytes();
            String nStr = new String(bText, UTF_8);
            out.println(nStr);
            out.flush();
        	if (out != null) {
            	out.close();
	        }
    	    if (!kkSocket.isClosed()) {
        	    kkSocket.close();
	        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}