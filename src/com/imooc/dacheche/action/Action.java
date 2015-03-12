package com.imooc.dacheche.action;

import com.imooc.dacheche.bean.ClientMessage;
import com.imooc.dacheche.bean.User;
import com.imooc.dacheche.common.InUtils;
import com.imooc.dacheche.common.OutUtils;
import com.imooc.dacheche.common.XmlUtils;
import com.imooc.dacheche.net.ClientNetManager;

/**
 * �ͻ����¼�����ӿ�
 * @author Huang Shan
 *
 */
public abstract class Action {

	/**
	 * ��ǰ��½�û���Ϣ
	 */
	private User user;
	/**
	 * �������
	 */
	private ClientNetManager net;
	
	protected User getUser() {
		return user;
	}
	protected ClientNetManager getNet() {
		return net;
	}
	
	public abstract void execute();
	
	/**
	 * �����û����ʹ���action����
	 * @param user
	 * @return
	 */
	public static Action newAction(User user, ClientNetManager net) {
		Action action = null;
		// �����ǰ��½���ǳ˿�,�����˿Ͳ�������
		if(user.getType() == 1) {
			action = new PassengerAction();
		}
		// �����˾��,�򴴽���Ӧ˾���Ķ���
		else {
			action = new DriverAction();
		}
		
		action.user = user;
		action.net = net;
		
		return action;
	}
	
	/**
	 * ע��
	 */
	protected void logout() {
		OutUtils.outMsg("ɾ��������Ϣ���˳�,���� /�� ���� /��: ");
		String command = InUtils.inputCommand("��,��");
		if(command.equals("��")) {
			OutUtils.outMsg("ע����������...");
			ClientMessage cm = new ClientMessage();
			cm.setCommand(ClientMessage.EXIT);
			getNet().sendMessage(cm);
			getNet().stop();

			OutUtils.outMsg("ɾ��������Ϣ...");
			XmlUtils.removeFromXML();

			OutUtils.outMsg("ɾ���ɹ�,�����˳�...");
			System.exit(0);
		}
	}
}
