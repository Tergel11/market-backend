package market.commerce.aws.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.FileUtil;
import market.commerce.aws.AwsProperties;
import market.commerce.model.FileData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Product image / document storage.
 *
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnExpression("!'${aws.access-key:}'.isEmpty()")
public class S3Service {

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    /**
     * @param folder logical prefix, e.g. "product" or "merchant"
     */
    public FileData upload(MultipartFile file, String folder) {
        String key = folder + "/" + FileUtil.uniqueName(file.getOriginalFilename());

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException ex) {
            log.error("Could not upload file to s3 : {}", key, ex);
            throw new UncheckedIOException(ex);
        }

        return FileData.builder()
                .key(key)
                .url(awsProperties.getS3().getCloudFrontUrl() + key)
                .name(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
    }

    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(key)
                .build());
        log.info("Deleted s3 object : {}", key);
    }
}
