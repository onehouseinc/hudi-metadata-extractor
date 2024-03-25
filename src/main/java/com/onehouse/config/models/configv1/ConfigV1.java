package com.onehouse.config.models.configv1;

import static com.onehouse.constants.MetadataExtractorConstants.TABLE_DISCOVERY_INTERVAL_MINUTES;
import static com.onehouse.constants.MetadataExtractorConstants.TABLE_METADATA_UPLOAD_INTERVAL_MINUTES;

import com.onehouse.config.Config;
import com.onehouse.config.ConfigVersion;
import com.onehouse.config.models.common.FileSystemConfiguration;
import com.onehouse.config.models.common.OnehouseClientConfig;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Jacksonized
@EqualsAndHashCode
public class ConfigV1 implements Config {
  @NonNull private String version;
  @NonNull private OnehouseClientConfig onehouseClientConfig;
  @NonNull private FileSystemConfiguration fileSystemConfiguration;

  // If metadataExtractorConfigPath is provided, it overrides metadataExtractorConfig. If not
  // provided, it is mandatory to pass metadataExtractorConfig.
  private String metadataExtractorConfigPath;
  private MetadataExtractorConfig metadataExtractorConfig;

  @Builder.Default private int tableDiscoveryIntervalMinutes = TABLE_DISCOVERY_INTERVAL_MINUTES;

  @Builder.Default
  private int tableMetadataUploadIntervalMinutes = TABLE_METADATA_UPLOAD_INTERVAL_MINUTES;

  @Override
  public ConfigVersion getVersion() {
    return ConfigVersion.valueOf(version);
  }
}
