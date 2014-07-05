package models.files;

import models.UserCredential;
import org.springframework.data.neo4j.annotation.NodeEntity;
import java.util.Set;

@NodeEntity
public class ImageFile extends ContentFile {

    public ImageFile(String name, String extension, String contentType, UserCredential ownerUser, Set<FileTransformation> fileTransforms) {

        if (fileTransforms.size() > 0) {
            this.fileTransformations = fileTransforms;
        }
        populateBaseData(name, extension, contentType, ownerUser);
    }

    public ImageFile(String name, String extension, String contentType, UserCredential ownerUser) {
        populateBaseData(name, extension, contentType, ownerUser);
    }

    private ImageFile() {
    }

    private void populateBaseData(String name, String extension, String contentType, UserCredential ownerUser) {
        this.owner = ownerUser;
        this.name = name;
        this.contentType = contentType;
        this.extension = extension;
    }
}
