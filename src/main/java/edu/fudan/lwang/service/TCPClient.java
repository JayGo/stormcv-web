package edu.fudan.lwang.service;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

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
	private static final Logger logger = Logger.getLogger(TCPClient.class);
	
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
			if(socket == null) {
				logger.info("socket is null");
			}
			
			logger.info("start to write socket...");
			oos = new ObjectOutputStream(socket.getOutputStream());
			
			oos.writeObject(msg);
			logger.info("finished write socket...");
			
			logger.info("start to read socket...");
			ios = new ObjectInputStream(socket.getInputStream());
//			while (null == result) {
			result=(BaseMessage) ios.readObject();
//			}
			logger.info("finished read socket...");
			
			
						
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