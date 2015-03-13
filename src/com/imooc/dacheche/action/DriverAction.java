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

	private boolean working = true;
	// 接收叫车请求的线程
	private ReceiveMsgThread rmt = null;
	private RequestMessage rm;
	
	@Override
	public void execute() {
		while(true) {
			OutUtils.outMsg("请输入 /上班  /查询记录  或者 /注销 命令:");
			// 接收
			String command = InUtils.inputCommand("上班,查询记录,注销");
			
			// 上班命令,将会不停接收乘客叫车消息并显示
			if(command.equals("上班")) {
				work();
			} 
			// 查询历史记录
			else if(command.equals("查询记录")) {
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
		while(working) {
			// 启动接收叫车消息线程
			if(rmt == null) {
				rmt = new ReceiveMsgThread();
			}
			new Thread(rmt).start();
			
			OutUtils.outMsg("上班状态,接收订单中,输入 /下班 停止接收。");
			// 接收司机输入命令并处理
			workCommand();
		}
	}
	
	/**
	 * 上班后命令处理,只能输入下班或抢单
	 */
	private void workCommand() {
		// 接收载客命令输入
		String command = InUtils.inputCommand("下班,抢单,取消");
		
		// 下班 停止接收乘客消息
		if(command.equals("下班")) {
			rmt.stopReceive();
			working = false;
			return;
		}
		
		// 取消 继续接收命令输入
		if(command.equals("取消")) {
			workCommand();
			return;
		}
		
		// 无单可抢 继续接收命令输入
		if(command.equals("抢单") && rm == null) {
			OutUtils.outMsg("当前无单可接,请继续等待...");
			workCommand();
			return;
		}
		
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
				OutUtils.outMsg("恭喜,乘客也相中了你,你们现在已经建立搭乘关系.");
				OutUtils.outMsg("到达终点别忘记提醒乘客带好行李物品.");
				
				// 到达终点时,跟乘客进行确认
				confirmDone(sm);
				break;
			// 乘客拒绝
			case ServerMessage.PASSENGER_REJECT: 
				OutUtils.outMsg("很遗憾,该乘客没有选择搭乘您的车,请不要气馁,继续加油.");
				work();
				break;
			// 没找到订单
			case ServerMessage.ORDER_NOT_FOUND: 
				OutUtils.outMsg("很遗憾,您晚了一步,该乘客已经被其他司机抢走了,请不要气馁,继续加油.");
				work();
		}
	}

	/**
	 * 等待乘客回应
	 */
	private void confirmDone(ServerMessage sm1) {
		// 输入到达命令
		OutUtils.outMsg("如已到达终点,请输入 /到达 命令来跟乘客进行确认:");
		InUtils.inputCommand("到达");
		
		// 发送到达消息给服务器端
		ClientMessage cm = new ClientMessage();
		cm.setCommand(1203);
		cm.setReceiver(sm1.getPassenger());
		getNet().sendMessage(cm);
		
		// 接收服务器端反馈
		ServerMessage sm = getNet().receiveMessage();
		
		if(sm.getState() == 2205) {
			OutUtils.outMsg("恭喜您完成任务,顺利到达终点,请等待下一位乘客的请求.");
		}
		
		else {
			OutUtils.outMsg("对不起,乘客认为您还没有到达终点,请继续行驶.");
			// 重新确认是否已经到达
			confirmDone(sm1);
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
	
	/**
	 * 接收叫车请求的线程
	 * @author Huang Shan
	 *
	 */
	class ReceiveMsgThread implements Runnable {
		
		private boolean getting = true;
		
		public void run() {
			getting = true;
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
				
				Utils.sleep(1000);
			}
		}
		
		/**
		 * 停止接收
		 */
		public void stopReceive() {
			getting = false;
		}
	}
}
