package com.onehouse.api.response;

import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class GenerateCommitMetadataUploadUrlResponse {
  @NonNull private final List<String> uploadUrls;
}
