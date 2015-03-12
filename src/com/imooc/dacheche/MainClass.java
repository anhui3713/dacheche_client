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
		
		OutUtils.outMsg("加载历史数据,请稍候...");
		// 从xml中加载之前已经登陆后的用户信息
		User user = XmlUtils.loadFromXML();
		// 如果用户信息为空,则证明之前没有登陆成功过,如果
		if(user == null) {
			// 提示需要输入登陆信息
			OutUtils.outMsg("您是第一次使用本应用,需要录入基本信息才可以继续使用.");
			// 接收用户输入个人信息
			user = InUtils.inputUser();
			// 保存到xml
			XmlUtils.saveToXML(user);
			
			OutUtils.outMsg("已将输入信息保存.");
		}
		OutUtils.outMsg("正在连接服务器...");
		
		// 构造登陆消息
		ClientMessage message = new ClientMessage();
		message.setCommand(ClientMessage.LOGIN);
		message.setSender(user);
		
		try {
			// 创建网络控制对象
			ClientNetManager net = new ClientNetManager();
			// 创建客户端操作处理对象
			Action action = Action.newAction(user, net);
			
			// 连接服务器端,并获取服务器端返回消息
			ServerMessage serm = net.connect(message);
			
			// 如果登陆成功
			if(serm.getState() == ServerMessage.LOGIN_OK) {
				// 将服务器端设置给客户端的id设置到user上
				user.setId(serm.getMessage());
				// 设置登陆时间为当前时间
				user.setLoginTime(new Date());
				// 登陆成功,提示消息,返回当前登陆用户的信息
				OutUtils.outMsg("登陆成功,[" + user.getName() + "]欢迎使用打车车.");
				
				// 执行action初始动作
				action.execute();
			} else {
				// 直接转到异常处理,退出程序
				throw new Exception();
			}
			
		} catch (Exception e) {
			// e.printStackTrace();
			OutUtils.outMsg("网络发生异常,请稍候重试.程序将在3秒后退出.");
			Utils.sleep(3000);
			System.exit(0);
		}
	}
}
