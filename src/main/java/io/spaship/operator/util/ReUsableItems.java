package io.spaship.operator.util;

import lombok.SneakyThrows;

public class ReUsableItems {

    private static final String SPA_MAPPING_WEBSITE_NAME_ATTRIBUTE = "websiteName";
    private static final String SPASHIP_MAPPING_FILE = ".spaship";


    private ReUsableItems() {
    }

    @SneakyThrows
    public static void blockFor(int timeInMs) {
        Thread.sleep(timeInMs);
    }


    public static String getSpaMappingWebsiteNameAttribute() {
        return SPA_MAPPING_WEBSITE_NAME_ATTRIBUTE;
    }

    public static String getSpashipMappingFileName() {
        return SPASHIP_MAPPING_FILE;
    }
}
