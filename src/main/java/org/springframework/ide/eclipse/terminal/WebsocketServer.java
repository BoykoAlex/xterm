/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package org.springframework.ide.eclipse.terminal;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.ide.eclipse.terminal.model.InitParams;
import org.springframework.ide.eclipse.terminal.model.Message;
import org.springframework.ide.eclipse.terminal.pty.PtyProcessInfo;
import org.springframework.ide.eclipse.terminal.pty.PtyProcessManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pty4j.WinSize;

@Controller
public class WebsocketServer implements WebSocketConfigurer, InitializingBean {
		
	private static final Logger log = LoggerFactory.getLogger(WebsocketServer.class);
	
	private Gson gson;

	private PtyProcessManager ptyProcessManager;
	
	private void initializeGson() {
		if (gson == null) {
			GsonBuilder builder = new GsonBuilder();
//			ActionTypeAdapter.configureGson(builder);
			gson = builder.create();
		}
	}
	
	public WebsocketServer(PtyProcessManager ptyProcessManager) {
		this.ptyProcessManager = ptyProcessManager;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initializeGson();
//		diagramServers.setRemoteEndpoint((fromServerId, message) -> {
//			sendMessageToClient(fromServerId, (JsonObject)gson.toJsonTree(message));
//		});
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(WsMessageHandler(), "/websocket")
		.setAllowedOrigins("*");
	}
	

	@Bean
	public WebSocketHandler WsMessageHandler() {
		return new TextWebSocketHandler() {
			
			@Override
			public void afterConnectionEstablished(WebSocketSession session) throws Exception {
				log.info("Websocket connection OPENED in: "+this);
			}

			@Override
			protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
				String payload = message.getPayload();
				log.info(payload);
					try {
					Message msg = Message.OBJECT_MAPPER.readValue(message.asBytes(), Message.class);
					PtyProcessInfo processInfo;
					switch (msg.getType()) {
					case Message.INIT_TYPE:
						String cwd = null;
						List<String> ptyParams = new ArrayList<>();
						ptyParams.add("--login");
						InitParams initParams = msg.getInitParams();
						if (initParams != null) {
							cwd = initParams.getCwd();
							if (initParams.getPtyParams() != null) {
								ptyParams.addAll(initParams.getPtyParams());
							}
							ptyParams = initParams.getPtyParams();
						}
			            processInfo = ptyProcessManager.createOrConnect(session, msg.getId(), cwd, ptyParams);
		                // Initial message for the session. Has the the session id only.
		                // Reply is send current pty content if any
			            session.sendMessage(new TextMessage(Message.dataMessage(msg.getId(), String.join("", processInfo.getBuffer())).toString()));
			            break;
					case Message.DATA_TYPE:
			            processInfo = ptyProcessManager.get(msg.getId());
			            if (processInfo != null) {
				            byte[] dataBytes = msg.getData().getBytes();
//				            ptyProcessManager.broadcastData(processInfo, dataBytes);
							processInfo.getPty().getOutputStream().write(dataBytes, 0, dataBytes.length);
							processInfo.getPty().getOutputStream().flush();
			            }
						break;
					case Message.SIZE_TYPE:
			            processInfo = ptyProcessManager.get(msg.getId());
			            if (processInfo != null) {
				            processInfo.getPty().setWinSize(new WinSize(msg.getSize().getCols(), msg.getSize().getRows()));
			            }
						break;
					default:
				        // Else it is a ping message with is just empty object {}
				        // Sent periodically from the client to keep the WS opened
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}

			@Override
			public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
				log.info("Websocket connection CLOSED in: "+this);
				ptyProcessManager.disconnectSocket(session);
			}
			
			@Override
			public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
				log.error("Websocket trasnport error: ", exception);
				ptyProcessManager.disconnectSocket(session);
			}
			
		};
	}
	

}
