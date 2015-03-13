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
 * 司机操作处理类
 * @author Huang Shan
 *
 */
public class DriverAction extends Action {

	private boolean flag = true;
	// 接收叫车请求的线程
	private ReceiveMsgThread rmt = null;
	private RequestMessage rm;
	
	@Override
	public void execute() {
		while(flag) {
			OutUtils.outMsg("请输入 /上班  /查询记录  或者 /注销 命令:");
			// 接收
			String command = InUtils.inputCommand("上班,查询记录,注销");
			
			// 上班命令,将会不停接收乘客叫车消息并显示
			if(command.equals("上班")) {
				work();
			} else if(command.equals("查询记录")) {
				showHistory();
			}
			// 退出
			else {
				logout();
			}
		}
	}

	/**
	 * 查看待打车乘客列表
	 */
	private void work() {
		// 启动接收叫车消息线程
		if(rmt == null) {
			rmt = new ReceiveMsgThread();
		}
		new Thread(rmt).start();
		
		OutUtils.outMsg("您现在处于上班状态，请等待乘客叫车请求，输入 /下班 进行其他操作。");
		// 接收司机输入命令并处理
		workCommand();
	}
	
	/**
	 * 上班后命令处理,只能输入下班或抢单
	 */
	private void workCommand() {
		// 接收载客命令输入
		String command = InUtils.inputCommand("下班,抢单");
		// 如果是输入抢单,并且
		if(command.equals("抢单")) {
			// 如果当前有可抢订单,则抢单
			if(rm != null) {
				// 构造抢单消息发送到客户端
				ClientMessage cm = new ClientMessage();
				cm.setCommand(ClientMessage.REQUEST_ORDER);
				cm.setMessage(rm.getUser().getId());
				getNet().sendMessage(cm);
				
				// 接收服务器端反馈内容
				ServerMessage sm = getNet().receiveMessage();
				switch(sm.getState()) {
					// 乘客接受
					case ServerMessage.PASSENGER_ACCEPT:
						// TODO
					// 乘客拒绝
					case ServerMessage.PASSENGER_REJECT: 
						// TODO
					// 没找到订单
					case ServerMessage.ORDER_NOT_FOUND: 
						// TODO
				}
			} 
			// 如果没有订单,则继续等待司机输入
			else {
				OutUtils.outMsg("当前无单可接,请继续等待...");
				// 继续接受命令输入
				workCommand();
			}
		}
		// 下班 停止接收乘客消息
		else if(command.equals("下班")) {
			rmt.stopReceive();
		}
	}

	/**
	 * 接收叫车请求的线程
	 * @author Huang Shan
	 *
	 */
	class ReceiveMsgThread implements Runnable {
		
		private boolean getting = true;
		
		public void run() {
			// 循环 接收服务器端消息
			while(getting) {
				// 主动发起获取当前乘客打车消息
				ClientMessage cm = new ClientMessage();
				cm.setCommand(ClientMessage.GET_ORDER);
				getNet().sendMessage(cm);
				
				// 接收服务器端返回内容
				ServerMessage sm = getNet().receiveMessage();
				
				// 获取一条乘客叫车请求
				rm = (RequestMessage) sm.getObjMsg();
				
				// 如果获取到消息，则显示并停止接收线程
				if(rm != null) {

					OutUtils.outMsg("==============新订单===============");
					OutUtils.outMsg("姓名：" + rm.getUser().getName());
					OutUtils.outMsg("性别：" + rm.getUser().getGender());
					OutUtils.outMsg("电话：" + rm.getUser().getPhone());
					OutUtils.outMsg("他说：" + rm.getMessage());
					OutUtils.outMsg("-----------------------------------");
					OutUtils.outMsg("输入 /抢单 或 /取消 来操作:");
					OutUtils.outMsg("===================================");
					
					// 结束获取订单的线程
					getting = false;
				}
			}
		}
		
		/**
		 * 停止接收
		 */
		public void stopReceive() {
			getting = false;
		}
	}
	
	/**
	 * 查看载客历史记录
	 */
	private void showHistory() {
		// 发送查看载客历史记录
		ClientMessage cm = new ClientMessage();
		cm.setCommand(ClientMessage.GET_HISTORY);
		getNet().sendMessage(cm);
		
		// 接收服务器反馈
		ServerMessage sm = getNet().receiveMessage();
		if(sm.getState() == ServerMessage.HISTORY) {
			@SuppressWarnings("unchecked")
			List<Log> logs = (List<Log>) sm.getObjMsg();
			OutUtils.outMsg("===================================================================");
			if(logs.size() > 0) {
				OutUtils.outln("序号\t乘客\t到达时间\t描述");
				
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
				OutUtils.outln("您当前还没有载客记录.");
			}
			OutUtils.outln("===================================================================");
			OutUtils.outMsg("您当前共有" + logs.size() + "次载客记录");
		}else {
			OutUtils.outMsg("服务器查询端遇到问题,工程师正在解决,请稍候再试...");
		}
	}
}
