package com.imooc.dacheche.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

import com.imooc.dacheche.bean.ClientMessage;
import com.imooc.dacheche.bean.ServerMessage;

/**
 * 客户端网络连接类
 * @author Huang Shan
 *
 */
public class ClientNetManager {
	
	/**
	 * 连接服务器端的套接字对象
	 */
	private Socket socket;
	/**
	 * 输出流
	 */
	private ObjectOutputStream oos;
	/**
	 * 输入流
	 */
	private ObjectInputStream ois;
	/**
	 * 网络连接状态,默认为未连接
	 */
	private boolean connecting = false;
	
	public ClientNetManager() {
		super();
	}

	/**
	 * 连接服务器端
	 * @param user
	 * @throws ConnectException 
	 */
	private void connect() throws ConnectException {
		try {
			// 连接服务器
			socket = new Socket("localhost", 9527);
			// 获取输入流
			ois = new ObjectInputStream(socket.getInputStream());
			//socket.set
			// 读取一个空对象,主要用于接收服务器端发送打通消息连接的对象
			try {
				ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			// 获取输出流,发送消息使用封装的sendMessage方法
			oos = new ObjectOutputStream(socket.getOutputStream());
			
			// 设置为连接状态
			connecting = true;
		} catch (IOException e) {
			// e.printStackTrace();
			throw new ConnectException();
		}	
	}
	
	/**
	 * 
	 * @param cm
	 * @return
	 * @throws IOException
	 * @throws ConnectException
	 */
	public ServerMessage connect(ClientMessage cm) throws IOException, ConnectException {
		
		// 连接服务器
		connect();
		// 发送消息到服务器端
		sendMessage(cm);
		// 接收服务器端消息并返回
		return receiveMessage();
	}
	
	/**
	 * 发送消息到服务器端
	 * @param msg
	 * @throws IOException 
	 * @throws ConnectException 
	 */
	public void sendMessage(ClientMessage cm) {
		try {
			cm.setSendTime(new Date());
			oos.writeObject(cm);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 接收服务器端返回消息
	 * @return
	 * @throws SocketTimeoutException 
	 */
	public ServerMessage receiveMessage(){
		try {
			Object obj = ois.readObject();
			ServerMessage serm = (ServerMessage) obj;
			
			return serm;
		} catch (ClassNotFoundException e) {
		} catch (SocketTimeoutException e) {
			return receiveMessage();
		} catch (IOException e) {
		}
		return null;
	}
	
	/**
	 * 停止
	 */
	public void stop() {
		if(connecting) {
			// TODO
		}
	}
}
