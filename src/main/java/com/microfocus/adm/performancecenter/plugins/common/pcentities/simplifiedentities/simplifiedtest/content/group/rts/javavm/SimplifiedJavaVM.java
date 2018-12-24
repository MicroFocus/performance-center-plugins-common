package com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.rts.javavm;

import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimplifiedJavaVM {

    private String jdk_home;

    private String java_vm_parameters;

    private boolean use_xboot;

    private boolean enable_classloader_per_vuser;

    private String[] java_env_class_paths;

    public SimplifiedJavaVM() { }

    public SimplifiedJavaVM(String jdk_home, String java_vm_parameters, boolean use_xboot, boolean enable_classloader_per_vuser, String[] java_env_class_paths) {
        this.jdk_home = jdk_home;
        this.java_vm_parameters = java_vm_parameters;
        this.use_xboot = use_xboot;
        this.enable_classloader_per_vuser = enable_classloader_per_vuser;
        this.java_env_class_paths = java_env_class_paths;
    }


    @Override
    public String toString() {
        return "SimplifiedJavaVM {" +
                "jdk_home = " + jdk_home +
                ", " + "java_vm_parameters = " + java_vm_parameters +
                ", " + "use_xboot = " + use_xboot +
                ", " + "enable_classloader_per_vuser = " + enable_classloader_per_vuser +
                ", " + "java_env_class_paths = " + java_env_class_paths +
                "}";
    }


    public String objectToXML() {
        XStream xstream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("_-", "_")));
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("SimplifiedJavaVM", SimplifiedJavaVM.class);
        xstream.aliasField("jdk_home", SimplifiedJavaVM.class, "jdk_home");
        xstream.aliasField("java_vm_parameters", SimplifiedJavaVM.class, "java_vm_parameters");
        xstream.aliasField("use_xboot", SimplifiedJavaVM.class, "use_xboot");
        xstream.aliasField("enable_classloader_per_vuser", SimplifiedJavaVM.class, "enable_classloader_per_vuser");

        xstream.alias("java_env_class_paths", String.class);
        xstream.addImplicitCollection(SimplifiedJavaVM.class, "java_env_class_paths", "java_env_class_paths", String.class);

        xstream.aliasField("SimplifiedJavaVM", SimplifiedJavaVM.class, "SimplifiedJavaVM");
        xstream.setMode(XStream.NO_REFERENCES);
        return xstream.toXML(this);
    }

    public static SimplifiedJavaVM xmlToObject(String xml) {
        XStream xstream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("_-", "_")));
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("SimplifiedJavaVM" , SimplifiedJavaVM.class);

        xstream.alias("java_env_class_paths", String.class);
        xstream.addImplicitCollection(SimplifiedJavaVM.class, "java_env_class_paths", "java_env_class_paths", String.class);

        xstream.setClassLoader(SimplifiedJavaVM.class.getClassLoader());
        xstream.setMode(XStream.NO_REFERENCES);
        return (SimplifiedJavaVM)xstream.fromXML(xml);
    }
}
