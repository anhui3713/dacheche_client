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

	private boolean flag = true;
	// ���սг�������߳�
	private ReceiveMsgThread rmt = null;
	private RequestMessage rm;
	
	@Override
	public void execute() {
		while(flag) {
			OutUtils.outMsg("������ /�ϰ�  /��ѯ��¼  ���� /ע�� ����:");
			// ����
			String command = InUtils.inputCommand("�ϰ�,��ѯ��¼,ע��");
			
			// �ϰ�����,���᲻ͣ���ճ˿ͽг���Ϣ����ʾ
			if(command.equals("�ϰ�")) {
				work();
			} else if(command.equals("��ѯ��¼")) {
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
		// �������սг���Ϣ�߳�
		if(rmt == null) {
			rmt = new ReceiveMsgThread();
		}
		new Thread(rmt).start();
		
		OutUtils.outMsg("�����ڴ����ϰ�״̬����ȴ��˿ͽг��������� /�°� ��������������");
		// ����˾�������������
		workCommand();
	}
	
	/**
	 * �ϰ�������,ֻ�������°������
	 */
	private void workCommand() {
		// �����ؿ���������
		String command = InUtils.inputCommand("�°�,����");
		// �������������,����
		if(command.equals("����")) {
			// �����ǰ�п�������,������
			if(rm != null) {
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
						// TODO
					// �˿;ܾ�
					case ServerMessage.PASSENGER_REJECT: 
						// TODO
					// û�ҵ�����
					case ServerMessage.ORDER_NOT_FOUND: 
						// TODO
				}
			} 
			// ���û�ж���,������ȴ�˾������
			else {
				OutUtils.outMsg("��ǰ�޵��ɽ�,������ȴ�...");
				// ����������������
				workCommand();
			}
		}
		// �°� ֹͣ���ճ˿���Ϣ
		else if(command.equals("�°�")) {
			rmt.stopReceive();
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
			}
		}
		
		/**
		 * ֹͣ����
		 */
		public void stopReceive() {
			getting = false;
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
}
