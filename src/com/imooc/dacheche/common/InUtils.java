package com.imooc.dacheche.common;

import java.util.Scanner;
import java.util.UUID;

import com.imooc.dacheche.bean.User;

/**
 * 输入工具类
 * @author Huang Shan
 *
 */
public class InUtils {

	/**
	 * 输入依赖的对象
	 */
	private static Scanner scanner = new Scanner(System.in);
	
	/**
	 * 接收一个消息内容
	 * @return 接收到的消息内容
	 */
	public static synchronized String inputMsg() {
		String msg = scanner.nextLine().trim();
		return msg;
	}
	
	/**
	 * 接收一个输入命令
	 * 命令格式为:/命令
	 * 需要提取 / 后的内容
	 * @return 命令值
	 */
	private static synchronized String inputCommand() {
		String inStr = inputMsg();
		while(!inStr.startsWith("/")) {
			OutUtils.outMsg("命令输入不正确,请重新输入:");
			inStr = scanner.nextLine().trim();
		}
		String command = inStr.substring(1);
		
		return command;
	}
	
	/**
	 * 接收一个输入命令
	 * 命令格式为:/命令
	 * 需要提取 / 后的内容
	 * @param receive 可接受的命令范围
	 * 
	 * @return 命令值
	 */
	public static synchronized String inputCommand(String receive) {
		String command = inputCommand();
		
		// 判断命令是否输入正确
		if(command.contains(",") || (!receive.contains(command))) {
			OutUtils.outMsg("命令输入不正确,请重新输入:");
			command = inputCommand();
		}
		
		return command;
	}
	
	/**
	 * 接收一个输入的数字内容
	 * @return
	 */
	public static synchronized int inputInt() {
		String msg = inputMsg();
		try {
			 return Integer.parseInt(msg);
		} catch (NumberFormatException e) {
			OutUtils.outMsg("您输入的不是一个数字,请重新输入: ");
			return inputInt();
		}
	}
	
	/**
	 * 接收一个输入的数字内容
	 * @param min 接收的最小值
	 * @param max 接收的最大值
	 * @return
	 */
	public static int inputInt(int min, int max) {
		int value = inputInt();
		
		if(value < min || value > max) {
			OutUtils.outMsg("请输入正确范围的数字:" );
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

		OutUtils.outMsg("请输入姓名:");
		String name = InUtils.inputMsg();
		
		OutUtils.outMsg("请输入性别:");
		String gender = InUtils.inputMsg();
		
		OutUtils.outMsg("请输入您的身份[1: 乘客,2: 司机]:");
		int type = InUtils.inputInt(1, 2);
		
		OutUtils.outMsg("请输入个人信息:");
		String remark = InUtils.inputMsg();
		
		user.setName(name);
		user.setGender(gender);
		user.setType(type);
		user.setRemark(remark);

		// 为第一次登入用户设置一个主键
		user.setId(UUID.randomUUID().toString());
		
		return user;
	}
}
