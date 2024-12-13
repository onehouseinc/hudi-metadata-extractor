package ai.onehouse.metadata_extractor;

import ai.onehouse.constants.MetricsConstants;
import ai.onehouse.exceptions.RateLimitException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static ai.onehouse.metadata_extractor.MetadataExtractorUtils.getMetadataExtractorFailureReason;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MetadataExtractorUtilsTest {

    @Test
    void testGetMetadataExtractorFailureReasonWithRateLimitException(){
        MetricsConstants.MetadataUploadFailureReasons reason = getMetadataExtractorFailureReason(
            new RateLimitException(""),
            MetricsConstants.MetadataUploadFailureReasons.UNKNOWN
        );
        assertEquals(MetricsConstants.MetadataUploadFailureReasons.RATE_LIMITING, reason);
    }

    @Test
    void testGetMetadataExtractorFailureReasonWithRuntimeException(){
        MetricsConstants.MetadataUploadFailureReasons reason = getMetadataExtractorFailureReason(
            new RuntimeException(""),
            MetricsConstants.MetadataUploadFailureReasons.HOODIE_PROPERTY_NOT_FOUND_OR_CORRUPTED
        );
        assertEquals(MetricsConstants.MetadataUploadFailureReasons.HOODIE_PROPERTY_NOT_FOUND_OR_CORRUPTED, reason);
    }
}
