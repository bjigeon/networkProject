package kr.hs.dgsw.test01.n2309.server;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread{

    static File filePath = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Server" + System.getProperty("file.separator"));

    static String fileName = "";

    static Socket socket;

    static DataInputStream dis;

    static DataOutputStream dos;

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        System.out.println(socket.getInetAddress() +"가 접속하였습니다.");
        dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.start();
    }

    public void run(){
        String line = null;

        try {
            login();
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }

        while (true) {
            // 클라에서 온 명령어를 해석하고 실행
            try {
                line = dis.readUTF();
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }


            if (line.equals("server Close")) {
                System.out.println(socket.getInetAddress() + "가 로그아웃 하였습니다");
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }finally {
                    break;
                }
            } else if (line.equals("show Files")) {
                try {
                    showFileList();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else {

                String[] Cmd = line.split(" ", 2);
                String[] filename = Cmd[0].split(System.getProperty("file.separator"));
                fileName = filename[filename.length - 1];
                String cmd = Cmd[1];

                System.out.println(fileName);

                try {
                    if (cmd.equals("upload")) {
                        uploadFile();
                    } else if (cmd.equals("download")) {
                        downloadFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 클라에서 보낸 로그인 정보를 확인하고 로그인 시킴
    public void login() throws IOException {
        while (true) {
            String[] IdPw = dis.readUTF().split(" ", 2);
            String id = IdPw[0];
            String pw = IdPw[1];

            if (id.equals("admin") && pw.equals("1234")) {
                System.out.println(socket.getInetAddress() +"가 로그인에 성공하였습니다.");
                dos.writeBoolean(true);
                dos.flush();
                break;
            }
            System.out.println(socket.getInetAddress() +"가 로그인에 실패하였습니다.");
            dos.writeBoolean(false);
            dos.flush();
        }
    }

    //서버에 있는 파일의 크기를 계산
    public String Size(File file){
        long size = file.length();
        int cnt = 0;
        String Size[] = {"Byte","KB","MB","GB","TB"};

        while (size > 1024) {
            size/=1024;
            cnt++;
        }

        return size + " " + Size[cnt];
    }

    // 서버에 있는 파일 목록 및 파일 크기를 보내줌
    public void showFileList() throws IOException {
        File ServerFile = filePath;
        File[] fileList = ServerFile.listFiles();

        dos.writeInt(fileList.length);
        for (File file : fileList){
            dos.writeUTF(String.valueOf(file) + "   " + Size(file));
            dos.flush();
        }
        System.out.println();
    }

    //클라 기준 업로드
    public static void uploadFile() throws IOException {

        File serverFile = new File(filePath + System.getProperty("file.separator") + fileName);

        long size = dis.readLong();

        byte[] bytes = new byte[5120];
        int readbit = 0;

        //파일이 서버에 있으면 클라로 파일이 있다고 보내줌
        if (serverFile.exists()){
            dos.writeUTF("exist");
            dos.flush();

            String an = dis.readUTF();
            if (an.equals("y") || an.equals("Y")){
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(serverFile));

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

                dos.writeUTF("파일을 덮어 썼습니다");
                dos.flush();

            }else {
                dos.writeUTF("파일을 덮어 쓰지 않았습니다");
                dos.flush();

            }

        }
        // 파일이 없으면 파일이 없다고 보내고 데이터를 전송 해달라고 보냄
        else {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(serverFile));
            dos.writeUTF("notexist");
            dos.flush();

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
        }


        System.out.println("파일 업로드를 완료하였습니다");
    }

    //클라 기준 다운
    //클라에서 다운을 요청한 파일을 보내줌
    public void downloadFile() throws IOException {

        filePath = new File(filePath + System.getProperty("file.separator") + fileName);

        dos.writeLong(filePath.length());

        BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(filePath)
        );

        byte[] bytes = new byte[5120];
        int readbit = -1;

        while ((readbit = bis.read(bytes)) > 0) {
            dos.write(bytes, 0, readbit);
        }
        dos.flush();
        bis.close();

        System.out.println("다운로드 완료");
    }

}

//           upload /Users/bjigeon/Desktop/aa.jpeg