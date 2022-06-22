package kr.hs.dgsw.test01.n2309.server;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerMain {
    public static void main(String args[]) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("서버 시작");
        while (true) {
            new ServerThread(serverSocket.accept());
        }
    }
}
