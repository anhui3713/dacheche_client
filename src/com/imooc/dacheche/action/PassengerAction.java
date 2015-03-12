package com.imooc.dacheche.action;

import com.imooc.dacheche.bean.ClientMessage;
import com.imooc.dacheche.bean.ServerMessage;
import com.imooc.dacheche.bean.User;
import com.imooc.dacheche.common.InUtils;
import com.imooc.dacheche.common.OutUtils;


/**
 * 针对乘客的操作处理类
 * @author Huang Shan
 *
 */
public class PassengerAction extends Action {

	private boolean flag = true;
	
	@Override
	public void execute() {
		while(flag) {
			OutUtils.outMsg("请输入 /叫车 或者 /注销 命令:");
			// 接收打车命令输入
			String command = InUtils.inputCommand("叫车,注销");
			
			// 叫车
			if(command.equals("叫车")) {
				getCar();
			} 
			// 注销
			else {
				logout();
			}
		}
	}
	
	/**
	 * 叫车
	 */
	private void getCar() {
		// 输入打车信息
		OutUtils.outMsg("输入你想对司机说的话:");
		String remark = InUtils.inputMsg();
		
		// 构造需要打车消息
		ClientMessage cm = new ClientMessage();
		cm.setCommand(ClientMessage.CALL_TAXI);
		cm.setMessage(remark);
		
		// 发送叫车请求
		getNet().sendMessage(cm);
		OutUtils.outMsg("您的请求已经发出,请稍候...");
		
		receive();
	}
	
	/**
	 * 接收到到司机回应
	 * @param sm
	 */
	public void receive() {
		// 接收服务器回复
		ServerMessage sm = getNet().receiveMessage();
		
		switch(sm.getState()) {
			// 匹配到司机
			case ServerMessage.DRIVER_REQUEST:
				// 调用接收或拒绝方法
				// 获取司机消息
				User driver = sm.getDriver();
				OutUtils.outMsg("您运气真好,这么快就有司机对您的消息做出回应,该司机信息如下:");
				// 显示司机信息
				OutUtils.outMsg("姓名:" + driver.getName());
				OutUtils.outMsg("性别:" + driver.getGender());
				OutUtils.outMsg("个人介绍:" + driver.getRemark());
				
				// 输入是否愿意接收
				OutUtils.outMsg("请输入 /是 /否 愿意搭乘该司机的车:");
				String command = InUtils.inputCommand("是,否");
				
				// 响应消息
				ClientMessage cm = new ClientMessage();
				cm.setCommand(command.equals("是") ? ClientMessage.ACCEPT : ClientMessage.REJECT);
				cm.setReceiver(driver);
				getNet().sendMessage(cm);
				if(command.equals("是")) {
					OutUtils.outMsg("成功!司机正在前往您的位置,祝您旅途愉快!");
					waitDone();
				} else {
					OutUtils.outMsg("您残忍的拒绝了该司机,那就只有再继续等待了...");
					receive();
				}
				break;
			// 超时
			case ServerMessage.CALL_TIMEOUT:
				OutUtils.outMsg("可能现在没有空闲的司机,或者没有司机愿意搭乘您,请重新尝试");
				break;
			// 其他情况
			default: 
				OutUtils.outMsg("服务器消息异常,请重试");
		}
		
	}
	
	/**
	 * 等待完成
	 */
	private void waitDone() {
		// 接收消息
		ServerMessage sm = getNet().receiveMessage();
		if(sm.getState() == ServerMessage.REQUEST_DONE) {
			OutUtils.outMsg("司机请求您确认是否到达目的地,输入 /是 /否 命令来确认: ");
			
			String command = InUtils.inputCommand("是,否");
			// 响应消息
			ClientMessage cm = new ClientMessage();
			cm.setCommand(command.equals("是") ? ClientMessage.ACCEPT_DONE : ClientMessage.REJECT_DONE);
			cm.setReceiver(sm.getDriver());
			getNet().sendMessage(cm);
			
			// 如果已经到达终点,则提示完成,否则继续等待司机请求是否到达
			if(command.equals("是")) {
				OutUtils.outMsg("恭喜您顺利到达目的地,感谢使用打车车应用,您可以选择继续打车或者注销登陆");
			} else {
				waitDone();
			}
		} else {
			System.out.println(sm);
		}
	}
}
