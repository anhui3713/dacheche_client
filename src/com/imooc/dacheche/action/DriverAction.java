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
 * 司机操作处理类
 * @author Huang Shan
 *
 */
public class DriverAction extends Action {

	private boolean flag = true;
	// 乘客叫车请求
	private Queue<ServerMessage> rms = new LinkedList<ServerMessage>();
	// 接收叫车请求的线程
	private ReceiveMsgThread rmt = null;
	
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
		OutUtils.outMsg("您现在处于上班状态，请等待乘客叫车请求，输入 /下班 进行其他操作。");
		
		// 启动接收叫车消息线程
		rmt = new ReceiveMsgThread();
		rmt.start();
		
		// 接收载客命令输入
		String command = InUtils.inputCommand("下班");
		// 下班 停止接收乘客消息
		if(command.equals("下班")) {
			rmt.stopReceive();
			rmt = null;
		}
	}
	
	

	/**
	 * 接收叫车请求的线程
	 * @author Huang Shan
	 *
	 */
	class ReceiveMsgThread extends Thread {
		
		private boolean running = true;
		
		public void run() {
			// 循环 接收服务器端消息
			while(running) {
				// 主动发起获取当前乘客打车消息
				ClientMessage cm = new ClientMessage();
				cm.setCommand(ClientMessage.GET_ORDER);
				getNet().sendMessage(cm);
				
				// 接收服务器端返回内容
				ServerMessage sm = getNet().receiveMessage();
				rms.add(sm);
			}
			
			// 清空已经接收到的叫车请求
			//rms.clear();
		}
		
		/**
		 * 停止接收
		 */
		public void stopReceive() {
			running = false;
		}
		
		/**
		 * 是否已经关闭接收线程
		 * @return
		 */
		public boolean isClosed() {
			return !running;
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
