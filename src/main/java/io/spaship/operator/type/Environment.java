package io.spaship.operator.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Environment {

    String name;
    String websiteName;
    UUID traceID;
    String nameSpace;
    boolean updateRestriction;
    Path zipFileLocation;
    String websiteVersion;
    String spaName;
    String spaContextPath;
    String branch;
    boolean excludeFromEnvironment; //to create or not to create :P
    boolean operationPerformed = false; // for flagging purpose, to know whether any k8s operation is performed

    @Override
    public String toString() {
        return "{"
                + "\"name\":\"" + name + "\""
                + ", \"websiteName\":\"" + websiteName + "\""
                + ", \"traceID\":" + traceID
                + ", \"nameSpace\":\"" + nameSpace + "\""
                + ", \"updateRestriction\":\"" + updateRestriction + "\""
                + ", \"zipFileLocation\":" + zipFileLocation
                + ", \"websiteVersion\":\"" + websiteVersion + "\""
                + ", \"spaName\":\"" + spaName + "\""
                + ", \"spaContextPath\":\"" + spaContextPath + "\""
                + ", \"branch\":\"" + branch + "\""
                + ", \"excludeFromEnvironment\":\"" + excludeFromEnvironment + "\""
                + ", \"operationPerformed\":\"" + operationPerformed + "\""
                + "}";
    }


}
