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

import com.thoughtworks.xstream.XStream;

public class PcRunResult {
	
    private int ID;
    
    private String Name;
    
    private int RunID;
    
    private String Type;
	
	public static PcRunResult xmlToObject(String xml)
    {   	  
  	  XStream xstream = new XStream();
  	  xstream.alias("RunResult" , PcRunResult.class);
  	  return (PcRunResult)xstream.fromXML(xml);	
    }

	public int getID() {
		return ID;
	}

	public String getName() {
		return Name;
	}

	public int getRunID() {
		return RunID;
	}

	public String getType() {
		return Type;
	}
	
	
}