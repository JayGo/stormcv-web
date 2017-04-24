package edu.fudan.lwang.service;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(TCPClient.class);
	
	public TCPClient(String serverAddr, int port) {
		try {
			socket = new Socket(serverAddr, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String sendRequest(String msg) {
		if (socket == null) {
			logger.info("socket is null");
		}

		String result = null;
		try {
			logger.info("start to write socket...");
			Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
			writer.write(msg);
			writer.flush();
			logger.info("finished write socket...");

			logger.info("start to read socket...");
			Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
			char chars[] = new char[128];
			int len;
			StringBuilder sb = new StringBuilder();
			while ((len = reader.read(chars)) != -1) {
				sb.append(new String(chars, 0, len));
			}
			result = sb.toString();
			logger.info("finished read socket, read:{}", result);
			
			writer.close();
			reader.close();
		} catch (EOFException e) {
			if (socket.isClosed()) {
				logger.error("Socket is closed now!!!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
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
				logger.error("Socket is closed now!!!");
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
	
	public void close() {
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}