package io.spaship.operator.type;

import io.vertx.core.json.JsonObject;

import java.util.List;

/**
* This is a specialised class for retrieving the .spaship mapping
* */
public class SpashipMapping extends JsonObject {

    SpashipMapping() {
        super();
    }

    public SpashipMapping(String param){
        super(param);
        init();
    }

    void init(){
        setWebsiteVersion();
        setWebsiteName();
        setEnvironments();
        setName();
        setExcludeFromEnvs();
        setBranch();
    }

    // we dont need any constant because we know the property name matches with incoming json attributes
    String websiteVersion;
    String websiteName;
    List<String> environments;
    //Name of the SPA
    String name;
    List<String> excludeFromEnvs;
    String branch;

    public String getWebsiteVersion() {
        return websiteVersion;
    }

    public void setWebsiteVersion() {
        this.websiteVersion = this.getString("websiteVersion");
    }

    public String getWebsiteName() {
        return websiteName;
    }

    public void setWebsiteName() {
        this.websiteName = this.getString("websiteName");
    }

    public List<String> getEnvironments() {
        return environments;
    }

    public void setEnvironments() {
        this.environments = this.getJsonArray("environments").getList();
    }

    public String getName() {
        return name;
    }

    public void setName() {
        this.name = this.getString("name");
    }

    public List<String> getExcludeFromEnvs() {
        return excludeFromEnvs;
    }

    public void setExcludeFromEnvs() {
        this.excludeFromEnvs = this.getJsonArray("excludeFromEnvs").getList();
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch() {
        this.branch = this.getString("branch");
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
