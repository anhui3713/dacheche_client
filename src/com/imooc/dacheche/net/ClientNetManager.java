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
 * �ͻ�������������
 * @author Huang Shan
 *
 */
public class ClientNetManager {
	
	/**
	 * ���ӷ������˵��׽��ֶ���
	 */
	private Socket socket;
	/**
	 * �����
	 */
	private ObjectOutputStream oos;
	/**
	 * ������
	 */
	private ObjectInputStream ois;
	/**
	 * ��������״̬,Ĭ��Ϊδ����
	 */
	private boolean connecting = false;
	
	public ClientNetManager() {
		super();
	}

	/**
	 * ���ӷ�������
	 * @param user
	 * @throws ConnectException 
	 */
	private void connect() throws ConnectException {
		try {
			// ���ӷ�����
			socket = new Socket("localhost", 9527);
			// ��ȡ������
			ois = new ObjectInputStream(socket.getInputStream());
			//socket.set
			// ��ȡһ���ն���,��Ҫ���ڽ��շ������˷��ʹ�ͨ��Ϣ���ӵĶ���
			try {
				ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			// ��ȡ�����,������Ϣʹ�÷�װ��sendMessage����
			oos = new ObjectOutputStream(socket.getOutputStream());
			
			// ����Ϊ����״̬
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
		
		// ���ӷ�����
		connect();
		// ������Ϣ����������
		sendMessage(cm);
		// ���շ���������Ϣ������
		return receiveMessage();
	}
	
	/**
	 * ������Ϣ����������
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
	 * ���շ������˷�����Ϣ
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
	 * ֹͣ
	 */
	public void stop() {
		if(connecting) {
			// TODO
		}
	}
}
