package com.imooc.dacheche.action;

import com.imooc.dacheche.bean.ClientMessage;
import com.imooc.dacheche.bean.User;
import com.imooc.dacheche.common.InUtils;
import com.imooc.dacheche.common.OutUtils;
import com.imooc.dacheche.common.XmlUtils;
import com.imooc.dacheche.net.ClientNetManager;

/**
 * 客户端事件处理接口
 * @author Huang Shan
 *
 */
public abstract class Action {

	/**
	 * 当前登陆用户信息
	 */
	private User user;
	/**
	 * 网络对象
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
	 * 根据用户类型创建action对象
	 * @param user
	 * @return
	 */
	public static Action newAction(User user, ClientNetManager net) {
		Action action = null;
		// 如果当前登陆的是乘客,创建乘客操作对象
		if(user.getType() == 1) {
			action = new PassengerAction();
		}
		// 如果是司机,则创建对应司机的对象
		else {
			action = new DriverAction();
		}
		
		action.user = user;
		action.net = net;
		
		return action;
	}
	
	/**
	 * 注销
	 */
	protected void logout() {
		OutUtils.outMsg("删除您的信息并退出,输入 /是 或者 /否: ");
		String command = InUtils.inputCommand("是,否");
		if(command.equals("是")) {
			OutUtils.outMsg("注销网络连接...");
			ClientMessage cm = new ClientMessage();
			cm.setCommand(ClientMessage.EXIT);
			getNet().sendMessage(cm);
			getNet().stop();

			OutUtils.outMsg("删除保存信息...");
			XmlUtils.removeFromXML();

			OutUtils.outMsg("删除成功,正在退出...");
			System.exit(0);
		}
	}
}
