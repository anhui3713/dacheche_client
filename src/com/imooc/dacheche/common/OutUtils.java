package com.imooc.dacheche.common;

/**
 * ���������
 * @author Huang Shan
 *
 */
public class OutUtils {

	public static synchronized void outln(String msg) {
		System.out.println(msg);
	}
	
	/**
	 * �����Ϣ������̨
	 * @param msg
	 */
	public static synchronized void outMsg(String msg) {
		System.out.println();
		System.out.println(msg);
	}
}
