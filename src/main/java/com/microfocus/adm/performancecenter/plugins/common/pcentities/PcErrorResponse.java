/**
 Copyright 2018 Micro Focus International plc

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.microfocus.adm.performancecenter.plugins.common.pcentities;

import com.microfocus.adm.performancecenter.plugins.common.rest.PcRestProxy;
import com.thoughtworks.xstream.XStream;

public class PcErrorResponse {

	@SuppressWarnings("unused")
    private String xmlns = PcRestProxy.PC_API_XMLNS;
	
	public String ExceptionMessage;
	
	public int ErrorCode;
		
	public PcErrorResponse(String exceptionMessage, int errorCode) {
		ExceptionMessage = exceptionMessage;
		ErrorCode = errorCode;
	}

	public static PcErrorResponse xmlToObject(String xml) {
		XStream xstream = new XStream();
		xstream.setClassLoader(PcErrorResponse.class.getClassLoader());
		xstream.alias("Exception", PcErrorResponse.class);
		return (PcErrorResponse) xstream.fromXML(xml);

	}
	
}
