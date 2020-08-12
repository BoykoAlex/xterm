package org.springframework.ide.eclipse.terminal.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Message {
	
	private static final Logger log = LoggerFactory.getLogger(Message.class);
	
	public static final String PING_TYPE = "ping";
	public static final String SIZE_TYPE = "size";
	public static final String DATA_TYPE = "data";
	public static final String INIT_TYPE = "init";
	
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.setSerializationInclusion(Include.NON_NULL)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	private String type;
	private String id;
	private String data;
	private List<String> cmd;
	private String cwd;
	private Size size;
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSession() {
		return id;
	}

	public void setSession(String session) {
		this.id = session;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Size getSize() {
		return size;
	}

	public void setSize(Size size) {
		this.size = size;
	}
	
	public List<String> getCmd() {
		return cmd;
	}

	public void setCmd(List<String> shellCommand) {
		this.cmd = shellCommand;
	}

	public String getCwd() {
		return cwd;
	}

	public void setCwd(String cwd) {
		this.cwd = cwd;
	}

	@Override
	public String toString() {
		try {
			return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			log.error("", e);
			return "";
		}
	}
	

	public static Message sizeMessage(String id, int cols, int rows) {
		Message msg = new Message();
		msg.setType(SIZE_TYPE);
		msg.setId(id);
		Size size = new Size();
		size.setCols(cols);
		size.setRows(rows);
		msg.setSize(size);
		return msg;
	}
	
	public static Message dataMessage(String id, String data) {
		Message msg = new Message();
		msg.setType(SIZE_TYPE);
		msg.setId(id);
		msg.setData(data);
		return msg;
	}
	
}
