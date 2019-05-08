import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.davejy.modelsimplification.ObjectModel;

public class Main {
	
	public static final int PORT_ADDR = 5000;

	public static void main(String[] args) {
		String filename = "dinosaur.2k.obj";
        ObjectModel om = new ObjectModel(filename);
//        om.print();
        om.simplifiedToRatio(0.1f);
//        float qave = om.calQAve();
//        float qmse = om.calQMSE(qave);
//        System.out.println("Q ave:" + qave);
//        System.out.println("Q mse:" + qmse);
        om.writeObjFile();
//        om.print();
        
//        try {
//			ServerSocket serverSocket = new ServerSocket(PORT_ADDR);
//			System.out.println("开始监听端口" + PORT_ADDR + "...");
//			while(true) {
//				Socket socket = serverSocket.accept();
//				System.out.println("客户端已连接");
//				om.writeTo(socket.getOutputStream());
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
