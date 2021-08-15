package io.spaship.operator.api;


import io.spaship.operator.service.SPAUploadHandler;
import io.spaship.operator.util.FormData;
import org.jboss.resteasy.reactive.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("upload")
public class SPAUploadController {

    private static final Logger LOG = LoggerFactory.getLogger(SPAUploadController.class);
    private final SPAUploadHandler spaUploadHandlerService;

    public SPAUploadController(SPAUploadHandler spaUploadHandlerService) {
        this.spaUploadHandlerService = spaUploadHandlerService;
    }

    @Produces("text/plain")
    @GET
    public String upload() {
        return "please post a spa zip in the same url to make it work";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadSPA(@MultipartForm FormData formData) {
        String description = logIncomingFormData(formData);
        spaUploadHandlerService.handleFileUpload(formData.getfilePath());
        return "SPA: " + description + " deployment In progress";
    }

    private String logIncomingFormData(FormData formData) {
        String description = formData.description;
        String fileName = formData.fileName();
        Long fileSize = formData.fileSize();
        java.nio.file.Path path = formData.getfilePath();
        LOG.debug("file received description {} , name is {} , size {}, location {} \n",
                description, fileName, fileSize, path);
        return description;
    }
}




