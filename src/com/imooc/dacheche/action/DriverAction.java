package com.imooc.dacheche.action;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.imooc.dacheche.bean.ClientMessage;
import com.imooc.dacheche.bean.Log;
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
	// �˿ͽг�����
	private Queue<ServerMessage> rms = new LinkedList<ServerMessage>();
	// ���սг�������߳�
	private ReceiveMsgThread rmt = null;
	
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
		OutUtils.outMsg("�����ڴ����ϰ�״̬����ȴ��˿ͽг��������� /�°� ��������������");
		
		// �������սг���Ϣ�߳�
		rmt = new ReceiveMsgThread();
		rmt.start();
		
		// �����ؿ���������
		String command = InUtils.inputCommand("�°�");
		// �°� ֹͣ���ճ˿���Ϣ
		if(command.equals("�°�")) {
			rmt.stopReceive();
			rmt = null;
		}
	}
	
	

	/**
	 * ���սг�������߳�
	 * @author Huang Shan
	 *
	 */
	class ReceiveMsgThread extends Thread {
		
		private boolean running = true;
		
		public void run() {
			// ѭ�� ���շ���������Ϣ
			while(running) {
				// ���������ȡ��ǰ�˿ʹ���Ϣ
				ClientMessage cm = new ClientMessage();
				cm.setCommand(ClientMessage.GET_ORDER);
				getNet().sendMessage(cm);
				
				// ���շ������˷�������
				ServerMessage sm = getNet().receiveMessage();
				rms.add(sm);
			}
			
			// ����Ѿ����յ��Ľг�����
			//rms.clear();
		}
		
		/**
		 * ֹͣ����
		 */
		public void stopReceive() {
			running = false;
		}
		
		/**
		 * �Ƿ��Ѿ��رս����߳�
		 * @return
		 */
		public boolean isClosed() {
			return !running;
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
