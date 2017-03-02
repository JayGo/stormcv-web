package edu.fudan.lwang.service;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.fudan.jliu.message.BaseMessage;

/**
 * send: "start,[videoAddress]" ---> received:
 * "succeed,[jpeg stream ip address and port] <strong>OR</strong> Error,[reason]"
 * <br>
 * send: "startByMaster,[videoAddress]" ---> received:
 * "succeed,[jpeg stream ip address and port] <strong>OR</strong> Error,[reason]"
 * <br>
 * send: "end,[videoAddress]" ---> received "Succeed" <strong>OR</strong> "Error,[reason]"
 * <br>
 * send: "endByMaster,[videoAddress]" ---> received "Succeed" <strong>OR</strong> "Error,[reason]"
 * 
 * @author lwang
 *
 */
public class TCPClient {

	private Socket socket;
	
	public TCPClient(String serverAddr, int port) {
		try {
			socket = new Socket(serverAddr, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public BaseMessage sendMsg(BaseMessage msg) {
		BaseMessage result = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ios = null;
		try {
			
			oos = new ObjectOutputStream(socket.getOutputStream());
			
			oos.writeObject(msg);
			
			ios = new ObjectInputStream(socket.getInputStream());
			while (null == result) {
				result=(BaseMessage) ios.readObject();
			}
						
//			oos.flush();
//			oos.close();
//			ios.close();
			
			// socket.close();
		} catch (EOFException e) {
			e.printStackTrace();
			if(socket.isClosed()) {
				System.out.println("Socket is closed now!!!");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
}