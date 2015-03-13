package com.imooc.dacheche.action;

import java.text.MessageFormat;
import java.util.List;

import com.imooc.dacheche.bean.ClientMessage;
import com.imooc.dacheche.bean.Log;
import com.imooc.dacheche.bean.RequestMessage;
import com.imooc.dacheche.bean.ServerMessage;
import com.imooc.dacheche.common.InUtils;
import com.imooc.dacheche.common.OutUtils;
import com.imooc.dacheche.common.Utils;

/**
 * ˾������������
 * @author Huang Shan
 *
 */
public class DriverAction extends Action {

	private boolean working = true;
	// ���սг�������߳�
	private ReceiveMsgThread rmt = null;
	private RequestMessage rm;
	
	@Override
	public void execute() {
		while(true) {
			OutUtils.outMsg("������ /�ϰ�  /��ѯ��¼  ���� /ע�� ����:");
			// ����
			String command = InUtils.inputCommand("�ϰ�,��ѯ��¼,ע��");
			
			// �ϰ�����,���᲻ͣ���ճ˿ͽг���Ϣ����ʾ
			if(command.equals("�ϰ�")) {
				work();
			} 
			// ��ѯ��ʷ��¼
			else if(command.equals("��ѯ��¼")) {
				showHistory();
			}
			// �˳�
			else {
				logout();
			}
		}
	}

	/**
	 * �鿴���򳵳˿��б�
	 */
	private void work() {
		while(working) {
			// �������սг���Ϣ�߳�
			if(rmt == null) {
				rmt = new ReceiveMsgThread();
			}
			new Thread(rmt).start();
			
			OutUtils.outMsg("�ϰ�״̬,���ն�����,���� /�°� ֹͣ���ա�");
			// ����˾�������������
			workCommand();
		}
	}
	
	/**
	 * �ϰ�������,ֻ�������°������
	 */
	private void workCommand() {
		// �����ؿ���������
		String command = InUtils.inputCommand("�°�,����,ȡ��");
		
		// �°� ֹͣ���ճ˿���Ϣ
		if(command.equals("�°�")) {
			rmt.stopReceive();
			working = false;
			return;
		}
		
		// ȡ�� ����������������
		if(command.equals("ȡ��")) {
			workCommand();
			return;
		}
		
		// �޵����� ����������������
		if(command.equals("����") && rm == null) {
			OutUtils.outMsg("��ǰ�޵��ɽ�,������ȴ�...");
			workCommand();
			return;
		}
		
		// ����������Ϣ���͵��ͻ���
		ClientMessage cm = new ClientMessage();
		cm.setCommand(ClientMessage.REQUEST_ORDER);
		cm.setMessage(rm.getUser().getId());
		getNet().sendMessage(cm);
		
		// ���շ������˷�������
		ServerMessage sm = getNet().receiveMessage();
		switch(sm.getState()) {
			// �˿ͽ���
			case ServerMessage.PASSENGER_ACCEPT:
				OutUtils.outMsg("��ϲ,�˿�Ҳ��������,���������Ѿ�������˹�ϵ.");
				OutUtils.outMsg("�����յ���������ѳ˿ʹ���������Ʒ.");
				
				// �����յ�ʱ,���˿ͽ���ȷ��
				confirmDone(sm);
				break;
			// �˿;ܾ�
			case ServerMessage.PASSENGER_REJECT: 
				OutUtils.outMsg("���ź�,�ó˿�û��ѡ�������ĳ�,�벻Ҫ����,��������.");
				work();
				break;
			// û�ҵ�����
			case ServerMessage.ORDER_NOT_FOUND: 
				OutUtils.outMsg("���ź�,������һ��,�ó˿��Ѿ�������˾��������,�벻Ҫ����,��������.");
				work();
		}
	}

	/**
	 * �ȴ��˿ͻ�Ӧ
	 */
	private void confirmDone(ServerMessage sm1) {
		// ���뵽������
		OutUtils.outMsg("���ѵ����յ�,������ /���� ���������˿ͽ���ȷ��:");
		InUtils.inputCommand("����");
		
		// ���͵�����Ϣ����������
		ClientMessage cm = new ClientMessage();
		cm.setCommand(1203);
		cm.setReceiver(sm1.getPassenger());
		getNet().sendMessage(cm);
		
		// ���շ������˷���
		ServerMessage sm = getNet().receiveMessage();
		
		if(sm.getState() == 2205) {
			OutUtils.outMsg("��ϲ���������,˳�������յ�,��ȴ���һλ�˿͵�����.");
		}
		
		else {
			OutUtils.outMsg("�Բ���,�˿���Ϊ����û�е����յ�,�������ʻ.");
			// ����ȷ���Ƿ��Ѿ�����
			confirmDone(sm1);
		}
	}
	
	/**
	 * �鿴�ؿ���ʷ��¼
	 */
	private void showHistory() {
		// ���Ͳ鿴�ؿ���ʷ��¼
		ClientMessage cm = new ClientMessage();
		cm.setCommand(ClientMessage.GET_HISTORY);
		getNet().sendMessage(cm);
		
		// ���շ���������
		ServerMessage sm = getNet().receiveMessage();
		if(sm.getState() == ServerMessage.HISTORY) {
			@SuppressWarnings("unchecked")
			List<Log> logs = (List<Log>) sm.getObjMsg();
			OutUtils.outMsg("===================================================================");
			if(logs.size() > 0) {
				OutUtils.outln("���\t�˿�\t����ʱ��\t����");
				
				for (int i = 0; i < logs.size(); i++) {
					if(i > 0) {
						OutUtils.outln("-------------------------------------------------------------------");
					}
					
					Log log = logs.get(i);
					
					String pStr = MessageFormat.format("{0}\t{1}\t{2}\t{3}",
							(i + 1),
							log.getPassenger(),
							Utils.format(log.getEndTime(), "yyyy-MM-dd HH:mm:ss"),
							log.getRemark());
					
					OutUtils.outln(pStr);
				}
			} else {
				OutUtils.outln("����ǰ��û���ؿͼ�¼.");
			}
			OutUtils.outln("===================================================================");
			OutUtils.outMsg("����ǰ����" + logs.size() + "���ؿͼ�¼");
		}else {
			OutUtils.outMsg("��������ѯ����������,����ʦ���ڽ��,���Ժ�����...");
		}
	}
	
	/**
	 * ���սг�������߳�
	 * @author Huang Shan
	 *
	 */
	class ReceiveMsgThread implements Runnable {
		
		private boolean getting = true;
		
		public void run() {
			getting = true;
			// ѭ�� ���շ���������Ϣ
			while(getting) {
				// ���������ȡ��ǰ�˿ʹ���Ϣ
				ClientMessage cm = new ClientMessage();
				cm.setCommand(ClientMessage.GET_ORDER);
				getNet().sendMessage(cm);
				
				// ���շ������˷�������
				ServerMessage sm = getNet().receiveMessage();
				
				// ��ȡһ���˿ͽг�����
				rm = (RequestMessage) sm.getObjMsg();
				
				// �����ȡ����Ϣ������ʾ��ֹͣ�����߳�
				if(rm != null) {

					OutUtils.outMsg("==============�¶���===============");
					OutUtils.outMsg("������" + rm.getUser().getName());
					OutUtils.outMsg("�Ա�" + rm.getUser().getGender());
					OutUtils.outMsg("�绰��" + rm.getUser().getPhone());
					OutUtils.outMsg("��˵��" + rm.getMessage());
					OutUtils.outMsg("-----------------------------------");
					OutUtils.outMsg("���� /���� �� /ȡ�� ������:");
					OutUtils.outMsg("===================================");
					
					// ������ȡ�������߳�
					getting = false;
				}
				
				Utils.sleep(1000);
			}
		}
		
		/**
		 * ֹͣ����
		 */
		public void stopReceive() {
			getting = false;
		}
	}
}
