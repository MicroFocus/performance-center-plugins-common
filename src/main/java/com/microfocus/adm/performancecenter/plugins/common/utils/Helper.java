package com.microfocus.adm.performancecenter.plugins.common.utils;

public class Helper {
    public static String[] GetLreServerAndTenant(String lreServer)
    {

        String delimiterSlash = "/";
        String delimiterQuestionMark = "\\?";
        String useDelimiter = delimiterSlash;
        String[] strServerAndTenant = {lreServer, ""};

        String theLreServer = lreServer;
        //replace for common mistakes
        if(lreServer != null && !lreServer.isEmpty()) {
            theLreServer = lreServer.toLowerCase().replace("http://", "");
            theLreServer = theLreServer.replace("https://", "");
            theLreServer = theLreServer.replace("/lre", "");
            theLreServer = theLreServer.replace("/site", "");
            theLreServer = theLreServer.replace("/loadtest", "");
            theLreServer = theLreServer.replace("/pcx", "");
            theLreServer = theLreServer.replace("/adminx", "");
            theLreServer = theLreServer.replace("/admin", "");
            theLreServer = theLreServer.replace("/login", "");
        }
        if(theLreServer != null && !theLreServer.isEmpty()) {
            if(theLreServer.contains("/"))
            {
                useDelimiter = delimiterSlash;
            }
            else if(theLreServer.contains("?"))
            {
                useDelimiter = delimiterQuestionMark;
            }
            String[] severTenantArray = theLreServer.split(useDelimiter);
            if(severTenantArray.length > 0) {
                strServerAndTenant[0] = severTenantArray[0];
                if(severTenantArray.length > 1) {
                    if (useDelimiter.equals(delimiterQuestionMark)) {
                        strServerAndTenant[1] = delimiterQuestionMark + severTenantArray[1];
                    } else {
                        strServerAndTenant[1] = severTenantArray[1];
                    }
                }
            }
        }
        return strServerAndTenant;
    }
}
