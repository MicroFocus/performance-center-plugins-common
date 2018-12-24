package com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.rts.jmeter;

import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimplifiedJMeter {

    private boolean start_measurements;

    private String jmeter_home_path;

    private int jmeter_min_port;

    private int jmeter_max_port;

    private String jmeter_additional_properties;

    public SimplifiedJMeter() { }


    public SimplifiedJMeter(boolean start_measurements, String jmeter_home_path,  int jmeter_min_port, int jmeter_max_port) {
        this.start_measurements = start_measurements;
        this.jmeter_home_path = jmeter_home_path;
        this.jmeter_min_port = jmeter_min_port;
        this.jmeter_max_port = jmeter_max_port;
    }

    public SimplifiedJMeter(boolean start_measurements, String jmeter_home_path,  int jmeter_min_port, int jmeter_max_port, String jmeter_additional_properties) {
        this.start_measurements = start_measurements;
        this.jmeter_home_path = jmeter_home_path;
        this.jmeter_min_port = jmeter_min_port;
        this.jmeter_max_port = jmeter_max_port;
        this.jmeter_additional_properties = jmeter_additional_properties;
    }

    @Override
    public String toString() {
        return "SimplifiedJMeter {" +
                "start_measurements = " + start_measurements +
                ", " + "jmeter_home_path = " + jmeter_home_path +
                ", " + "jmeter_min_port = " + jmeter_min_port +
                ", " + "jmeter_max_port = " + jmeter_max_port +
                ", " + "jmeter_additional_properties = " + jmeter_additional_properties +
                "}";
    }

    public String objectToXML() {
        XStream xstream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("_-", "_")));
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("SimplifiedJMeter", SimplifiedJMeter.class);
        xstream.aliasField("start_measurements", SimplifiedJMeter.class, "start_measurements");
        xstream.aliasField("jmeter_home_path", SimplifiedJMeter.class, "jmeter_home_path");
        xstream.aliasField("jmeter_min_port", SimplifiedJMeter.class, "jmeter_min_port");
        xstream.aliasField("jmeter_max_port", SimplifiedJMeter.class, "jmeter_max_port");
        xstream.aliasField("jmeter_additional_properties", SimplifiedJMeter.class, "jmeter_additional_properties");

        xstream.aliasField("SimplifiedJMeter", SimplifiedJMeter.class, "SimplifiedJMeter");
        xstream.setMode(XStream.NO_REFERENCES);
        return xstream.toXML(this);
    }

    public static SimplifiedJMeter xmlToObject(String xml) {
        XStream xstream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("_-", "_")));
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("SimplifiedJMeter" , SimplifiedJMeter.class);

        xstream.setClassLoader(SimplifiedJMeter.class.getClassLoader());
        xstream.setMode(XStream.NO_REFERENCES);
        return (SimplifiedJMeter)xstream.fromXML(xml);
    }

}
