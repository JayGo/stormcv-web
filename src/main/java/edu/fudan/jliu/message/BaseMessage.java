package edu.fudan.jliu.message;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BaseMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int code;
	private String addr;
	private String rtmpAddr;
	private String streamId;
	
	public BaseMessage() {
		
	}
	
	public BaseMessage(int code, String addr, String rtmpAddr, String streamId) {
		this.code = code;
		this.addr = addr;
		this.rtmpAddr = rtmpAddr;
		this.streamId = streamId;
	}
	
	public BaseMessage(int code) {
		this(code, null);
	}
	
	public BaseMessage(int code, String addr, String rtmpAddr) {
		this(code, addr, rtmpAddr,null);
	}
	
	public BaseMessage(int code, String addr) {
		this(code, addr, null,null);
	}
	
	public BaseMessage(String streamId, int code, String addr) {
		this(code, addr, null,streamId);
	}
	
	public BaseMessage(String streamId, String rtmpAddr) {
		// TODO Auto-generated constructor stub
		this(streamId, -1000, rtmpAddr);
	}
	
	public BaseMessage(String streamId, String rtmpAddr, String addr) {
		this(-1000,addr,rtmpAddr,streamId);
	}
	
	@XmlElement(name = "code")
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	@XmlElement(name = "addr")
	public String getAddr() {
		return this.addr;
	}
	
	public void setAddr(String addr) {
		this.addr = addr;
	}
	
	@XmlElement(name = "rtmpAddr")
	public String getRtmpAddr() {
		return this.rtmpAddr;
	}
	
	public void setRtmpAddr(String rtmpAddr) {
		this.rtmpAddr = rtmpAddr;
	}

	@XmlElement(name = "streamId")
	public String getStreamId() {
		return this.streamId;
	}
	
	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(" code: "+code);
		sBuilder.append(" addr: "+addr);
		sBuilder.append(" rtmpAddr: "+rtmpAddr);
		sBuilder.append(" streamId: "+streamId);
		return sBuilder.toString();
	}
}