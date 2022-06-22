package client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class ClientMain {
    public static Scanner scanner = new Scanner(System.in);
    public static PrintWriter pw = null;

    public static boolean loginCheck;

    public static boolean login(PrintWriter pw, BufferedReader br) throws IOException {
        int count = 0;
        String id;
        String password;

        while(true) {
            System.out.print("아이디를 입력하세요: ");
            id = scanner.nextLine();
            System.out.print("비밀번호를 입력하세요: ");
            password = scanner.nextLine();

            pw.println("LOGIN>" + id + " " + password);
            String req = br.readLine();

            if(req.equals("true")){
                System.out.println("로그인에 성공하였습니다.");
                break;
            }
            System.out.println("다시 시도해주세요.");
            count++;
            if(count >= 5){
                System.out.println("로그인에 실패하였습니다.");
                return false;
            }
        }
        return true;
    }

    public static String makeToken(String operator){
        if(!operator.substring(0, 1).equals("/")){
            if(operator.equals("exit")){
                return "exit>";
            }
            System.out.println("명령어가 아님");
        }
        if(operator.contains("/파일목록")){
            if(operator.contains(" ")) {
                String[] tokenList = operator.split(" ");
                String token = tokenList[1];
                return "파일목록>img/"+token;
            }
            return "파일목록>img";
        }
        if(operator.contains("/업로드")){
            if(operator.contains(" ")){
                String[] tokenList = operator.split(" ");
                File fileToUpload = new File(tokenList[1]);

                if(tokenList.length >= 4){
                    System.out.println("잘못된 인수의 갯수 입니다.");
                    return "error>params";
                }
                File checkEx = new File(tokenList[1]);
                if(!checkEx.exists()){
                    System.out.println("없는 파일입니다.");
                    return "error>notFile";
                }
                if(tokenList.length == 2){
                    String[] filePath = tokenList[tokenList.length-1].split("/");
                    File file = new File(String.valueOf(tokenList[tokenList.length-1]));
                    System.out.println(file.length() + " " + file.getName());
                    return "업로드>"+tokenList[tokenList.length-1]+" "+filePath[filePath.length-1]+" "+file.length();
                }
                if(tokenList.length == 3){
                    return "업로드>"+tokenList[tokenList.length-2]+" "+tokenList[tokenList.length-1];
                }
            }
            System.out.println("파일 경로가 없습니다.");
            return "error>undefinedPath";
        }
        if(operator.contains("/다운로드")){
            if(operator.contains(" ")){
                String[] tokenList = operator.split(" ");
                String filename = tokenList[1];
                return "다운로드>"+filename;
            }else{
                System.out.println("잘못된 인수 갯수");
                return "error>undefinedFilename";
            }
        }
        return "error>notDefined";
    }

    public static void printList(String list){
        String[] file = list.split(" ");
        for (String f:file){
            System.out.println(f);
        }
    }

    public static void fileUpload(String list, Socket sc, BufferedReader br) {
        try {
            String[] token = list.split(" ");
            File file = new File(token[1]);
            FileInputStream fis = new FileInputStream(file);


            OutputStream os = sc.getOutputStream();
            BufferedOutputStream bor = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bor);

            if (token[0].equals("true")) {
                System.out.println("이미 파일이 존재합니다");
                System.out.print("덮어 씌우시겠습니까?");
                String overwrite = scanner.nextLine();


                if (!"Yes".equals(overwrite)) {
                    System.out.println("취소되었습니다.");
                    pw.println("no");
                    return;
                }
                pw.println("yes");
            }

//        byte[] bytes = new byte[1024];
//        int readbit = 0;
//        while ((readbit = fis.read(bytes)) != -1) {
//            // 파일 전송
//            dos.write(bytes, 0, readbit);
//        }
//        System.out.println(file.getName() + " 파일을 업로드하였습니다.");
//        dos.flush();

            if (token[0].equals("true")) {
                pw.println("true");
                pw.flush();
                byte[] bytes = new byte[1024];
                int readbit = 0;
                int cnt = 0;
                int sum = 0;
                System.out.println(file.length());
                while ((readbit = fis.read(bytes)) != -1 && sum < file.length()) {
                    // 파일 전송
                    sum += readbit;
                    System.out.println(sum + " " + file.length());
                    dos.write(bytes, 0, readbit);
                }

                System.out.println(file.getName() + " 파일을 업로드하였습니다.");
                dos.flush();
            } else {
                pw.println("true");
                pw.flush();
                byte[] bytes = new byte[1024];
                int readbit = 0;
                int sum = 0;
                System.out.println(file.length());
                while ((readbit = fis.read(bytes)) != -1 && sum < file.length()) {
                    // 파일 전송
                    sum += readbit;
                    System.out.println(sum + " " + file.length());
                    dos.write(bytes, 0, readbit);
                }

                System.out.println(file.getName() + " 파일을 업로드하였습니다.");
                dos.flush();
            }
            pw.flush();
            dos.flush();
            return;
        }catch (IOException e){
            System.out.println("server 종료로 업로드 실패");
        }
    }

    public static void download(String bool, Socket sc, BufferedReader br) throws IOException {
        String[] token = bool.split(" ");
        if(token[0].equals("true")){
            byte[] bytes = new byte[1024];
            int readbit = 0;
            InputStream is = sc.getInputStream();
            BufferedInputStream bir = new BufferedInputStream(is);
            DataInputStream dis = new DataInputStream(bir);

            File file = new File("/Users/seonghun/공부/spring/network/src/kr/hs/dgsw/network/test01/n2302/client/"+token[1]);

            FileOutputStream fos = new FileOutputStream(file);
            while ((readbit = dis.read(bytes)) != -1) {
                // 파일 전송
                fos.write(bytes, 0, readbit);
                if(readbit != 1024){
                    break;
                }
            }
            fos.flush();
            System.out.println(file.getName() + "파일이 다운로드 되었습니다");
        }
    }

    public static void tokenParser(String opertaotr, Socket sc, BufferedReader br) throws IOException {
        String[] pasingArray = opertaotr.split(">");
        String type = pasingArray[0];
        switch (type){
            case "fileList":
                printList(pasingArray[1]);
                break;
            case "fileUpload":
                fileUpload(pasingArray[1], sc, br);
                break;
            case "download":
                download(pasingArray[1], sc, br);
                break;
            case "exit":
                sc.close();
                loginCheck = false;
                break;
            default:
                break;
        }
        return;
    }

    public static void main(String[] args) {
        try {
            Socket sc = new Socket("192.168.249.8",5050);
            OutputStream os = sc.getOutputStream();
            pw = new PrintWriter(os, true);

            DataOutputStream dos = new DataOutputStream(os);
            InputStream is = sc.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            loginCheck = login(pw, br);

            while(loginCheck){
                String opertaor = scanner.nextLine();
                opertaor = makeToken(opertaor);
                pw.println(opertaor);
                pw.flush();
                tokenParser(br.readLine(), sc, br);
            }
        }catch (SocketException e){
            System.out.println("");
            System.out.println("server가 닫혔습니다");
            System.out.println("명령어 처리 실패");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
