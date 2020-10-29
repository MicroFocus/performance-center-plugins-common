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

public class PcRunRequest {

	@SuppressWarnings("unused")
    private String xmlns = PcRestProxy.PC_API_XMLNS;

	private int TestID;

	private int TestInstanceID;

	private int TimeslotID;

	private com.microfocus.adm.performancecenter.plugins.common.pcentities.TimeslotDuration TimeslotDuration;

	private String PostRunAction;

	private boolean VudsMode;
		

	public PcRunRequest(
			int testID,
			int testInstanceID,
			int timeslotID,
			TimeslotDuration timeslotDuration,
			String postRunAction,
			boolean vudsMode) {
		
		TestID = testID;
		TestInstanceID = testInstanceID;
		TimeslotID = timeslotID;
		TimeslotDuration = timeslotDuration;
		PostRunAction = postRunAction;
		VudsMode = vudsMode;
	}
	
	public PcRunRequest() {}

	public String objectToXML() {
		XStream obj = new XStream();
		obj.alias("Run", PcRunRequest.class);
		obj.alias("TimeslotDuration", TimeslotDuration.class);
		obj.useAttributeFor(PcRunRequest.class, "xmlns");
		return obj.toXML(this);
	}
	
	public int getTestID() {
		return TestID;
	}
	
	public int getTestInstanceID() {
		return TestInstanceID;
	}
	
	public int getTimeslotID() {
		return TimeslotID;
	}
	
	public TimeslotDuration getTimeslotDuration() {
		return TimeslotDuration;
	}
	
	public String getPostRunAction() {
		return PostRunAction;
	}
	
	public boolean isVudsMode() {
		return VudsMode;
	}
}
