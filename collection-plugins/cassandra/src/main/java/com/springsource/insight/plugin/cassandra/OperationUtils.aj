/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.cassandra;

import java.net.Socket;
import java.nio.ByteBuffer;

import org.apache.cassandra.thrift.TCustomSocket;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.SourceCodeLocation;


public privileged aspect OperationUtils {
	public static String putTransportInfo(Operation operation, TProtocol tprotocol) {
		TTransport trans=tprotocol.getTransport();
		
		if (trans instanceof TFramedTransport) {
			trans=((TFramedTransport)trans).transport_;
		}
		
		// get transport info
		String server=null;
		if ((trans instanceof TSocket) || (trans instanceof TCustomSocket)) {
			Socket socket;
			String host;
			int port;
			
			if (trans instanceof TSocket) {
				TSocket lsocket=(TSocket)trans;
				
				host=lsocket.host_;
				port=lsocket.port_;
				socket=lsocket.socket_;
			}
			else {
				TCustomSocket lsocket=(TCustomSocket)trans;
				
				host=lsocket.host;
				port=lsocket.port;
				socket=lsocket.socket;
			}
			
			if (host!=null) {
				server=host+":"+port;
			}
			else {
				server=socket.getInetAddress().getHostAddress()+":"+socket.getPort();
			}
		}
		else
		if (trans instanceof THttpClient) {
			server=((THttpClient)trans).url_.toString();
		}

		operation.putAnyNonEmpty("server", server);		
		return server;
	}
	
	public static Operation createOperation(OperationCollectionTypes opType, String label, SourceCodeLocation srcCodeLocation) {
		return new Operation().type(opType.type).label(opType.label+(label!=null?label:"")).sourceCodeLocation(srcCodeLocation);
	}
	
	public static String getText(byte[] data) {
		return (data!=null)?new String(data):"null";
	}
	
	public static String getText(String data) {
		return (data!=null)?data:"null";
	}
	
	public static String getText(ByteBuffer bbuf) {
		return (bbuf!=null)?new String(bbuf.array()):"null";
	}
	
	public static String getString(ByteBuffer bbuf) {
		return (bbuf!=null)?new String(bbuf.array()):null;
	}
	
	public static String getString(byte[] data) {
		return (data!=null)?new String(data):null;
	}
	
	public static String getAnyData(byte[] data) {
		return (data!=null)?new String(data):null;
	}
}
