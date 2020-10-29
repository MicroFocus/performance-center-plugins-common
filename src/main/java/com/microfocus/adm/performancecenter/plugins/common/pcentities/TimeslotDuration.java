package com.microfocus.adm.performancecenter.plugins.common.pcentities;

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
public class TimeslotDuration {

    private int Hours;

    private int Minutes;

    public TimeslotDuration(int hours, int minutes) {

        Hours = hours + minutes / 60;
        Minutes = minutes % 60;
    }

    public TimeslotDuration(String hours, String minutes) {
        
        try{
            int m = Integer.parseInt(minutes);
            int h = Integer.parseInt(hours) + m / 60;
            if (h < 480) {
                Hours = h;
                Minutes = m % 60;
            } else {
                Hours = 480;
                Minutes = 0;                
            }
        } catch (Exception e) {
            Hours = 0;
            Minutes = 0;
        }
    }

    public TimeslotDuration(int minutes) {

        this(0, minutes);
    }

    public int getHours() {

        return Hours;
    }

    public int getMinutes() {

        return Minutes;
    }

    public int toMinutes() {
        
        return Hours * 60 + Minutes;
    }

    @Override
    public String toString() {

        return String.format("%d:%02d(h:mm)", Hours, Minutes);
    }

}
