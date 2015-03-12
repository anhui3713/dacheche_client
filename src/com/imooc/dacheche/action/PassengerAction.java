package com.imooc.dacheche.action;

import com.imooc.dacheche.bean.ClientMessage;
import com.imooc.dacheche.bean.ServerMessage;
import com.imooc.dacheche.bean.User;
import com.imooc.dacheche.common.InUtils;
import com.imooc.dacheche.common.OutUtils;


/**
 * ��Գ˿͵Ĳ���������
 * @author Huang Shan
 *
 */
public class PassengerAction extends Action {

	private boolean flag = true;
	
	@Override
	public void execute() {
		while(flag) {
			OutUtils.outMsg("������ /�г� ���� /ע�� ����:");
			// ���մ���������
			String command = InUtils.inputCommand("�г�,ע��");
			
			// �г�
			if(command.equals("�г�")) {
				getCar();
			} 
			// ע��
			else {
				logout();
			}
		}
	}
	
	/**
	 * �г�
	 */
	private void getCar() {
		// �������Ϣ
		OutUtils.outMsg("���������˾��˵�Ļ�:");
		String remark = InUtils.inputMsg();
		
		// ������Ҫ����Ϣ
		ClientMessage cm = new ClientMessage();
		cm.setCommand(ClientMessage.CALL_TAXI);
		cm.setMessage(remark);
		
		// ���ͽг�����
		getNet().sendMessage(cm);
		OutUtils.outMsg("���������Ѿ�����,���Ժ�...");
		
		receive();
	}
	
	/**
	 * ���յ���˾����Ӧ
	 * @param sm
	 */
	public void receive() {
		// ���շ������ظ�
		ServerMessage sm = getNet().receiveMessage();
		
		switch(sm.getState()) {
			// ƥ�䵽˾��
			case ServerMessage.DRIVER_REQUEST:
				// ���ý��ջ�ܾ�����
				// ��ȡ˾����Ϣ
				User driver = sm.getDriver();
				OutUtils.outMsg("���������,��ô�����˾����������Ϣ������Ӧ,��˾����Ϣ����:");
				// ��ʾ˾����Ϣ
				OutUtils.outMsg("����:" + driver.getName());
				OutUtils.outMsg("�Ա�:" + driver.getGender());
				OutUtils.outMsg("���˽���:" + driver.getRemark());
				
				// �����Ƿ�Ը�����
				OutUtils.outMsg("������ /�� /�� Ը���˸�˾���ĳ�:");
				String command = InUtils.inputCommand("��,��");
				
				// ��Ӧ��Ϣ
				ClientMessage cm = new ClientMessage();
				cm.setCommand(command.equals("��") ? ClientMessage.ACCEPT : ClientMessage.REJECT);
				cm.setReceiver(driver);
				getNet().sendMessage(cm);
				if(command.equals("��")) {
					OutUtils.outMsg("�ɹ�!˾������ǰ������λ��,ף����;���!");
					waitDone();
				} else {
					OutUtils.outMsg("�����̵ľܾ��˸�˾��,�Ǿ�ֻ���ټ����ȴ���...");
					receive();
				}
				break;
			// ��ʱ
			case ServerMessage.CALL_TIMEOUT:
				OutUtils.outMsg("��������û�п��е�˾��,����û��˾��Ը������,�����³���");
				break;
			// �������
			default: 
				OutUtils.outMsg("��������Ϣ�쳣,������");
		}
		
	}
	
	/**
	 * �ȴ����
	 */
	private void waitDone() {
		// ������Ϣ
		ServerMessage sm = getNet().receiveMessage();
		if(sm.getState() == ServerMessage.REQUEST_DONE) {
			OutUtils.outMsg("˾��������ȷ���Ƿ񵽴�Ŀ�ĵ�,���� /�� /�� ������ȷ��: ");
			
			String command = InUtils.inputCommand("��,��");
			// ��Ӧ��Ϣ
			ClientMessage cm = new ClientMessage();
			cm.setCommand(command.equals("��") ? ClientMessage.ACCEPT_DONE : ClientMessage.REJECT_DONE);
			cm.setReceiver(sm.getDriver());
			getNet().sendMessage(cm);
			
			// ����Ѿ������յ�,����ʾ���,��������ȴ�˾�������Ƿ񵽴�
			if(command.equals("��")) {
				OutUtils.outMsg("��ϲ��˳������Ŀ�ĵ�,��лʹ�ô򳵳�Ӧ��,������ѡ������򳵻���ע����½");
			} else {
				waitDone();
			}
		} else {
			System.out.println(sm);
		}
	}
}
