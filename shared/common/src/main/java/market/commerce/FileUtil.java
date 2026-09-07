package market.commerce;

import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * @author Tergel
 */
public class FileUtil {

    public static final List<String> IMAGE_CONTENT_TYPES =
            List.of("image/jpeg", "image/jpg", "image/png", "image/webp");

    public static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    private FileUtil() {
    }

    public static boolean isImage(MultipartFile file) {
        return file != null && IMAGE_CONTENT_TYPES.contains(file.getContentType());
    }

    public static String extension(String fileName) {
        if (ObjectUtils.isEmpty(fileName) || !fileName.contains("."))
            return "";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    public static String uniqueName(String originalFileName) {
        String extension = extension(originalFileName);
        String name = UUID.randomUUID().toString().replace("-", "");
        return extension.isEmpty() ? name : name + "." + extension;
    }
}
