package com.imooc.dacheche;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import com.imooc.dacheche.action.Action;
import com.imooc.dacheche.bean.ClientMessage;
import com.imooc.dacheche.bean.ServerMessage;
import com.imooc.dacheche.bean.User;
import com.imooc.dacheche.common.InUtils;
import com.imooc.dacheche.common.OutUtils;
import com.imooc.dacheche.common.Utils;
import com.imooc.dacheche.common.XmlUtils;
import com.imooc.dacheche.net.ClientNetManager;

public class MainClass {
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		
		OutUtils.outMsg("������ʷ����,���Ժ�...");
		// ��xml�м���֮ǰ�Ѿ���½����û���Ϣ
		User user = XmlUtils.loadFromXML();
		// ����û���ϢΪ��,��֤��֮ǰû�е�½�ɹ���,���
		if(user == null) {
			// ��ʾ��Ҫ�����½��Ϣ
			OutUtils.outMsg("���ǵ�һ��ʹ�ñ�Ӧ��,��Ҫ¼�������Ϣ�ſ��Լ���ʹ��.");
			// �����û����������Ϣ
			user = InUtils.inputUser();
			// ���浽xml
			XmlUtils.saveToXML(user);
			
			OutUtils.outMsg("�ѽ�������Ϣ����.");
		}
		OutUtils.outMsg("�������ӷ�����...");
		
		// �����½��Ϣ
		ClientMessage message = new ClientMessage();
		message.setCommand(ClientMessage.LOGIN);
		message.setSender(user);
		
		try {
			// ����������ƶ���
			ClientNetManager net = new ClientNetManager();
			// �����ͻ��˲����������
			Action action = Action.newAction(user, net);
			
			// ���ӷ�������,����ȡ�������˷�����Ϣ
			ServerMessage serm = net.connect(message);
			
			// �����½�ɹ�
			if(serm.getState() == ServerMessage.LOGIN_OK) {
				// �������������ø��ͻ��˵�id���õ�user��
				user.setId(serm.getMessage());
				// ���õ�½ʱ��Ϊ��ǰʱ��
				user.setLoginTime(new Date());
				// ��½�ɹ�,��ʾ��Ϣ,���ص�ǰ��½�û�����Ϣ
				OutUtils.outMsg("��½�ɹ�,[" + user.getName() + "]��ӭʹ�ô򳵳�.");
				
				// ִ��action��ʼ����
				action.execute();
			} else {
				// ֱ��ת���쳣����,�˳�����
				throw new Exception();
			}
			
		} catch (Exception e) {
			// e.printStackTrace();
			OutUtils.outMsg("���緢���쳣,���Ժ�����.������3����˳�.");
			Utils.sleep(3000);
			System.exit(0);
		}
	}
}
