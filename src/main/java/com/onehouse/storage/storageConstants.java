package com.onehouse.storage;

import java.util.regex.Pattern;

public class storageConstants {
  // typical s3 path: "s3://bucket-name/path/to/object"
  public static final Pattern S3_PATH_PATTERN = Pattern.compile("^s3://([^/]+)/.*");
  // gcs path format "gs:// [bucket] /path/to/file"
  public static final Pattern GCS_PATH_PATTERN = Pattern.compile("^gs://([^/]+)/.*");
  // https://cloud.google.com/compute/docs/naming-resources#resource-name-format
  public static final String GCP_RESOURCE_NAME_FORMAT = "^[a-z]([-a-z0-9]*[a-z0-9])$";
}
