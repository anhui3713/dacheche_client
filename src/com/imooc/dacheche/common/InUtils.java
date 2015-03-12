package com.imooc.dacheche.common;

import java.util.Scanner;
import java.util.UUID;

import com.imooc.dacheche.bean.User;

/**
 * ���빤����
 * @author Huang Shan
 *
 */
public class InUtils {

	/**
	 * ���������Ķ���
	 */
	private static Scanner scanner = new Scanner(System.in);
	
	/**
	 * ����һ����Ϣ����
	 * @return ���յ�����Ϣ����
	 */
	public static synchronized String inputMsg() {
		String msg = scanner.nextLine().trim();
		return msg;
	}
	
	/**
	 * ����һ����������
	 * �����ʽΪ:/����
	 * ��Ҫ��ȡ / �������
	 * @return ����ֵ
	 */
	private static synchronized String inputCommand() {
		String inStr = inputMsg();
		while(!inStr.startsWith("/")) {
			OutUtils.outMsg("�������벻��ȷ,����������:");
			inStr = scanner.nextLine().trim();
		}
		String command = inStr.substring(1);
		
		return command;
	}
	
	/**
	 * ����һ����������
	 * �����ʽΪ:/����
	 * ��Ҫ��ȡ / �������
	 * @param receive �ɽ��ܵ����Χ
	 * 
	 * @return ����ֵ
	 */
	public static synchronized String inputCommand(String receive) {
		String command = inputCommand();
		
		// �ж������Ƿ�������ȷ
		if(command.contains(",") || (!receive.contains(command))) {
			OutUtils.outMsg("�������벻��ȷ,����������:");
			command = inputCommand();
		}
		
		return command;
	}
	
	/**
	 * ����һ���������������
	 * @return
	 */
	public static synchronized int inputInt() {
		String msg = inputMsg();
		try {
			 return Integer.parseInt(msg);
		} catch (NumberFormatException e) {
			OutUtils.outMsg("������Ĳ���һ������,����������: ");
			return inputInt();
		}
	}
	
	/**
	 * ����һ���������������
	 * @param min ���յ���Сֵ
	 * @param max ���յ����ֵ
	 * @return
	 */
	public static int inputInt(int min, int max) {
		int value = inputInt();
		
		if(value < min || value > max) {
			OutUtils.outMsg("��������ȷ��Χ������:" );
			return inputInt(min, max);
		}
		
		return value;
	}
	
	/**
	 * 
	 * @return
	 */
	public static User inputUser() {
		
		User user = new User();

		OutUtils.outMsg("����������:");
		String name = InUtils.inputMsg();
		
		OutUtils.outMsg("�������Ա�:");
		String gender = InUtils.inputMsg();
		
		OutUtils.outMsg("�������������[1: �˿�,2: ˾��]:");
		int type = InUtils.inputInt(1, 2);
		
		OutUtils.outMsg("�����������Ϣ:");
		String remark = InUtils.inputMsg();
		
		user.setName(name);
		user.setGender(gender);
		user.setType(type);
		user.setRemark(remark);

		// Ϊ��һ�ε����û�����һ������
		user.setId(UUID.randomUUID().toString());
		
		return user;
	}
}
