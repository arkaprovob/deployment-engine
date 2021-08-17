package io.spaship.operator.api;


import io.spaship.operator.repo.SharedRepository;
import io.spaship.operator.service.SPAUploadHandler;
import io.spaship.operator.type.FormData;
import lombok.SneakyThrows;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jboss.resteasy.reactive.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

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
        var response = sanity(formData);
        var fileUploadParams = new Triplet<>(formData.getfilePath(), response.getValue1(), formData.website);

        SharedRepository.enqueue(formData.website, new Pair<>(response.getValue1(), LocalDateTime.now()));
        spaUploadHandlerService.handleFileUpload(fileUploadParams);
        return response.toString();
    }


    @GET
    @Path("/enqueue/{website}")
    @Produces("text/plain")
    public Boolean enqueue(@PathParam("website") String website) {
        String uniqueTracing = UUID.randomUUID().toString();
        return SharedRepository.enqueue(website, new Pair<>(uniqueTracing, LocalDateTime.now()));
    }

    @GET
    @Path("/dequeue/{website}")
    @Produces("text/plain")
    public Boolean dequeue(@PathParam("website") String website) {
        return SharedRepository.dequeue(website);
    }

    private Pair<String, String> sanity(FormData formData) {
        String description = formData.description;
        String fileName = formData.fileName();
        Long fileSize = formData.fileSize();
        java.nio.file.Path path = formData.getfilePath();

        Objects.requireNonNull(description, "description is missing from the request body");
        Objects.requireNonNull(fileName, "file name not found");
        Objects.requireNonNull(fileSize, "file size cannot be null");
        Objects.requireNonNull(path, "unable to store the file");

        LOG.debug("file received description {} , name is {} , size {}, location {} \n",
                description, fileName, fileSize, path);
        String processId = UUID.randomUUID().toString();
        return new Pair<>(description, processId);
    }

    @SneakyThrows
    private void rateLimiter(int ms) {
        Thread.sleep(ms);
    }
}




