package com.imooc.dacheche.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.imooc.dacheche.bean.User;

/**
 * 针对XML操作的工具类
 * @author Huang Shan
 *
 */
public class XmlUtils {

	private static File getFile() {
		// System.getProperty("java.class.path")
		String folder = ClassLoader.getSystemResource("").getPath();
		String file = folder + "UserInfo.xml";
		
		return new File(file);
	}
	
	/**
	 * 删除用户信息
	 */
	public static void removeFromXML() {
		File file = getFile();
		if(file != null && file.exists()) {
			file.delete();
		}
	}
	
	/**
	 * 从xml配置中加载用户信息
	 * @return
	 */
	public static User loadFromXML() {
		User user = null;
		
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new FileInputStream(getFile()));
			Element element = document.getDocumentElement();
			
			// 从文档中获取用户信息结点
			NodeList users = element.getElementsByTagName("user");
			
			// 如果没有找到则当作之前没有登陆过
			if(users.getLength() == 0) {
				return null;
			}
			
			user = new User();
			// 取出第一个
			Node node = users.item(0);
			// 取出所有属性
			NamedNodeMap nnm = node.getAttributes();
			
			// 取出name
			String id = nnm.getNamedItem("id").getNodeValue();
			String name = nnm.getNamedItem("name").getNodeValue();
			String gender = nnm.getNamedItem("gender").getNodeValue();
			String typeStr = nnm.getNamedItem("type").getNodeValue();
			Integer type = Integer.parseInt(typeStr);
			String remark = nnm.getNamedItem("remark").getNodeValue();
			
			// 将数据封装到对象
			user.setId(id);
			user.setName(name);
			user.setGender(gender);
			user.setType(type);
			user.setRemark(remark);
			
		} catch (Exception e) {
			//e.printStackTrace();
			// 解析出错或没找到文件,则算作当前没有登陆过
			return null;
		}
		
		return user;
	}
	
	/**
	 * 将用户信息保存到xml中
	 * @param user
	 */
	public static void saveToXML(User user) {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		TransformerFactory tf = TransformerFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			
			builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element usersNode = document.createElement("users");
			Element userNode = document.createElement("user");

			userNode.setAttribute("id", user.getName());
			userNode.setAttribute("name", user.getName());
			userNode.setAttribute("gender", user.getGender());
			userNode.setAttribute("type", "" + user.getType());
			userNode.setAttribute("remark", user.getRemark());
			
			usersNode.appendChild(userNode);
			document.appendChild(usersNode);
			Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            PrintWriter pw = new PrintWriter(new FileOutputStream(getFile()));
            StreamResult result = new StreamResult(pw);
            
            transformer.transform(source, result);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
