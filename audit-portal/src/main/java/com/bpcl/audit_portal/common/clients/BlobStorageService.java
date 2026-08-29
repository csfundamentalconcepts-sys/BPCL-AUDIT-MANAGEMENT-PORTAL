package com.bpcl.audit_portal.common.clients;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class BlobStorageService {

    private final BlobContainerClient containerClient;

    public BlobStorageService(BlobContainerClient containerClient) {
        this.containerClient = containerClient;
    }

    public void uploadFile(
            MultipartFile file,
            String filename){
        try{

            BlobClient blobClient =
                    containerClient.getBlobClient(filename);

            blobClient.upload(
                    file.getInputStream(),
                    file.getSize(),
                    true
            );
        }
        catch(IOException e){
            throw new BAMPException(Errors.INTERNAL_ISSUE);
        }
    }
    public void deleteFile(String blobName) {

        BlobClient blobClient =
                containerClient.getBlobClient(blobName);

        blobClient.deleteIfExists();
    }
}