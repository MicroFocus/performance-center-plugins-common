package com.microfocus.adm.performancecenter.plugins.common.utils;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.automatictrending.AutomaticTrending;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.elasticcontrollerconfiguration.ElasticControllerConfiguration;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.elasticloadgeneratorconfiguration.ElasticLoadGeneratorConfiguration;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.lgdistribution.LGDistribution;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.workloadtype.WorkloadType;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.LGDistributionTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.VusersDistributionModeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.WorkloadTypeSubTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.WorkloadTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.automatictrending.SimplifiedAutomaticTrending;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.elasticconfiguration.SimplifiedElasticConfiguration;

final class ContentPartsFactory {

    String getController(SimplifiedContent simplifiedContent) {
        String controller = null;
        if (simplifiedContent.getController() != null && !simplifiedContent.getController().isEmpty()) {
            controller = simplifiedContent.getController();
            if (controller.equalsIgnoreCase("Elastic")) {
                controller = "Elastic";
            }
        }
        return controller;
    }

    WorkloadType getWorkloadType() {
        return new WorkloadType(WorkloadTypeValues.BASIC, VusersDistributionModeValues.BY_NUMBER, WorkloadTypeSubTypeValues.BY_TEST);
    }

    LGDistribution getLgDistribution(SimplifiedContent simplifiedContent) {
        LGDistributionTypeValues lgDistributionTypeValues = LGDistributionTypeValues.ALL_TO_EACH_GROUP;
        boolean isLGHostDefinedInGroups =
                simplifiedContent.getGroup().stream().filter(o -> o.getLg_name() != null && o.getLg_name().length > 0).count()
                        == simplifiedContent.getGroup().size();

        if (simplifiedContent.getLg_amount() == 0 && isLGHostDefinedInGroups) {
            lgDistributionTypeValues = LGDistributionTypeValues.MANUAL;
        } else if (simplifiedContent.getLg_amount() == 0) {
            simplifiedContent.setLg_amount(1);
        }

        LGDistribution lgDistribution = new LGDistribution(lgDistributionTypeValues);
        lgDistribution.setType(lgDistributionTypeValues);
        if (lgDistributionTypeValues == LGDistributionTypeValues.ALL_TO_EACH_GROUP) {
            lgDistribution.setAmount(simplifiedContent.getLg_amount());
        }
        return lgDistribution;
    }

    AutomaticTrending getAutomaticTrending(SimplifiedContent simplifiedContent) {
        SimplifiedAutomaticTrending simplifiedAutomaticTrending = simplifiedContent.getAutomatic_trending();
        if (simplifiedAutomaticTrending != null) {
            return new AutomaticTrending(simplifiedAutomaticTrending.getReport_id(), simplifiedAutomaticTrending.getMax_runs_in_report());
        }
        return null;
    }

    ElasticLoadGeneratorConfiguration getElasticLoadGeneratorConfiguration(SimplifiedContent simplifiedContent) {
        SimplifiedElasticConfiguration simplifiedElasticConfiguration = simplifiedContent.getLg_elastic_configuration();
        if (simplifiedElasticConfiguration != null && simplifiedElasticConfiguration.getImage_id() != 0) {
            return new ElasticLoadGeneratorConfiguration(
                    simplifiedElasticConfiguration.getImage_id(),
                    simplifiedElasticConfiguration.getMemory_limit(),
                    simplifiedElasticConfiguration.getCpu_limit());
        }
        return null;
    }

    ElasticControllerConfiguration getElasticControllerConfiguration(SimplifiedContent simplifiedContent) {
        SimplifiedElasticConfiguration simplifiedElasticConfiguration = simplifiedContent.getController_elastic_configuration();
        if (simplifiedElasticConfiguration != null && simplifiedElasticConfiguration.getImage_id() != 0) {
            return new ElasticControllerConfiguration(
                    simplifiedElasticConfiguration.getImage_id(),
                    simplifiedElasticConfiguration.getMemory_limit(),
                    simplifiedElasticConfiguration.getCpu_limit());
        }
        return null;
    }
}

