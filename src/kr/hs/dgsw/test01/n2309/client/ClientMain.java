package kr.hs.dgsw.test01.n2309.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;

public class ClientMain {
    Socket socket = new Socket("127.0.0.1",9999);

    static File filePath = new File("");

    String fileName = "";

    Scanner scanner = new Scanner(System.in);

    DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

    //전역 변수에 예외 처리를 위한 코드
    public ClientMain() throws IOException {
    }

    //로그인 아이디 및 비번 확인
    public boolean login() throws IOException {
        while (true) {
            System.out.println("아이디와 비밀번호를 입력하세요");

            System.out.printf("아이디 : ");
            String ID = scanner.nextLine().trim();

            System.out.printf("비번 : ");
            String PW = scanner.nextLine().trim();

            String IdPw = ID + " " +PW;
            dos.writeUTF(IdPw);
            dos.flush();

            if (dis.readBoolean()){
                return true;
            }else {
                System.out.printf("\n아이디 비번을 다시 입력하세요\n\n");
            }
        }
    }

    //업로드를 할때 뒤에 업로드 할 이름 넣을시 그 이름을 구분 하기 위한 코드
    public void setFileNameAndPath(String path){
        String[] tmpPath;

        // 빈칸 있음
        if (path.contains(" ")){
            tmpPath = path.split(" ",2);

            fileName = tmpPath[1];
            filePath = new File(tmpPath[0]);
        }
        //빈칸 없음
        else {
            tmpPath = path.split(System.getProperty("user.dir"));
//            tmpPath = path.split(filePath.pathSeparator);

            fileName = tmpPath[tmpPath.length - 1];
            filePath = new File(path);
        }
    }

    //사용법 출력 및 명령어 받기
    public void showMenuAndReceiveCmd() throws IOException {
        while (true){
            //메뉴 출력
            System.out.println("ls : 파일 목록 보기 (서버에 저장된 파일 목록)");
            System.out.println("upload : 파일 업로드 (예 : upload 파일경로 파일명  OR  upload 파일경로)");
            System.out.println("download : 파일 다운로드 (서버에 저장된 파일) (예 : download 파일명)");
            System.out.println("disconnect : 접속 종료");

            //명령어 받기
            String Cmd = scanner.nextLine();

            if (Cmd.equals("ls") || Cmd.equals("LS")) {
                showFileList();
            }
            else if (Cmd.equals("disconnect") || Cmd.equals("DISCONNECT")) {
                disconnect();
                break;
            }
            else{
                String[] tmpCmd = Cmd.split(" ", 2);
                if (tmpCmd[0].equals("upload") || tmpCmd[0].equals("UPLOAD")) {
                    setFileNameAndPath(tmpCmd[1]);
                    uploadFile();
                } else if (tmpCmd[0].equals("download") || tmpCmd[0].equals("DOWNLOAD")) {
                    download(tmpCmd[1]);
                }
            }

        }
    }

    //서버와 연결 종료
    public void disconnect() throws IOException {
        System.out.println("연결을 종료합니다.");
        dos.writeUTF("server Close");
        dos.flush();
        socket.close();
    }

    //명령어 ls를 첬을때 서버에서 부터 받는 코드를 출력
    public void showFileList() throws IOException {
        dos.writeUTF("show Files");
        dos.flush();

        int len = dis.readInt();
        for (int i = 0; i < len; i ++){
            System.out.println(dis.readUTF());
        }
        System.out.println();
    }

    //파일 경로를 받아 경로에 있는 파일을 서버로 보냄
    public void uploadFile() throws IOException {
        //클라에 파일이 있는가 없는가 확인
        if (filePath.exists()){
            System.out.println("파일과 보낼 파일명이 확인되었습니다. 파일 전송을 시작합니다.");

            dos.writeUTF(fileName + " " + "upload");
            dos.flush();

            dos.writeLong(filePath.length());
            dos.flush();

            //서버에 똑같은 이름이 있는지 없는지 확인하여 덮어 쓰기를 결정
            String ex =  dis.readUTF();
            if (ex.equals("exist")){
                System.out.println("파일이 이미 존재 합니다. 파일을 덮어 쓰겠습니까? Yes/No");

                String an = scanner.nextLine();
                dos.writeUTF(an);
                dos.flush();

                if (an.equals("y") || an.equals("Y")){
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));

                    byte[] bytes = new byte[5120];
                    int readbit = -1;

                    while ((readbit = bis.read(bytes)) > 0) {
                        dos.write(bytes, 0, readbit);
                    }
                    dos.flush();
                    bis.close();
                }

                System.out.println(dis.readUTF());
            }
            // 파일이 존재하지 않는다면 업로드
            else if(ex.equals("notexist")){
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));

                byte[] bytes = new byte[5120];
                int readbit = -1;

                while ((readbit = bis.read(bytes)) > 0) {
                    dos.write(bytes, 0, readbit);
                }
                dos.flush();
                bis.close();
            }

        }
        //파일 경로가 잘못 되었을 경우
        else {
            System.out.println("파일 경로가 잘못 되었습니다. 다시 입력하세요.");
        }
    }

    //서버에 있는 파일을 받아 오는 코드
    public void download(String filename) throws IOException {
        fileName = filename;
        dos.writeUTF(fileName + " " + "download");
        dos.flush();

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("/Users/bjigeon/Desktop/" + fileName));

        long size = dis.readLong();
        byte[] bytes = new byte[5120];
        int readbit = 0;

        while (true) {
            readbit = dis.read(bytes);
            bos.write(bytes, 0, readbit);
            size -= readbit;
            if (size <= 0) {
                break;
            }
        }
        bos.flush();
        bos.close();

        System.out.println("파일 다운로드를 완료하였습니다");

    }

    public static void main(String[] args) throws IOException {
        System.out.println("클라이언트 시작");

        ClientMain c = new ClientMain();

        if (c.login()){
            c.showMenuAndReceiveCmd();
        }
    }
}


//        upload /Users/bjigeon/Desktop/aa.jpeg
//        upload /Users/bjigeon/Desktop/m.MP4
//        download cc.jpeg