package com.onehouse.storage;

import static com.onehouse.storage.StorageConstants.LIST_API_FILE_LIMIT;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.inject.Inject;
import com.onehouse.storage.models.File;
import com.onehouse.storage.providers.GcsClientProvider;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCSAsyncStorageClient implements AsyncStorageClient {
  private final GcsClientProvider gcsClientProvider;
  private final StorageUtils storageUtils;
  private final ExecutorService executorService;
  private static final Logger logger = LoggerFactory.getLogger(GCSAsyncStorageClient.class);

  @Inject
  public GCSAsyncStorageClient(
      @Nonnull GcsClientProvider gcsClientProvider,
      @Nonnull StorageUtils storageUtils,
      @Nonnull ExecutorService executorService) {
    this.gcsClientProvider = gcsClientProvider;
    this.storageUtils = storageUtils;
    this.executorService = executorService;
  }

  @Override
  public CompletableFuture<List<File>> listFiles(String gcsUrl) {
    return CompletableFuture.supplyAsync(
        () -> {
          logger.debug(String.format("listing files in %s", gcsUrl));
          Bucket bucket =
              gcsClientProvider.getGcsClient().get(storageUtils.getGcsBucketNameFromPath(gcsUrl));
          String prefix = storageUtils.getPathFromUrl(gcsUrl);

          // ensure prefix which is not the root dir always ends with "/"
          prefix = !Objects.equals(prefix, "") && !prefix.endsWith("/") ? prefix + "/" : prefix;
          Iterable<Blob> blobs =
              bucket
                  .list(
                      Storage.BlobListOption.prefix(prefix),
                      Storage.BlobListOption.delimiter("/"),
                      Storage.BlobListOption.pageSize(LIST_API_FILE_LIMIT))
                  .iterateAll();
          String finalPrefix = prefix;
          return StreamSupport.stream(blobs.spliterator(), false)
              .map(
                  blob ->
                      File.builder()
                          .filename(blob.getName().replaceFirst(finalPrefix, ""))
                          .lastModifiedAt(
                              Instant.ofEpochMilli(!blob.isDirectory() ? blob.getCreateTime() : 0))
                          .isDirectory(blob.isDirectory())
                          .build())
              .collect(Collectors.toList());
        },
        executorService);
  }

  public CompletableFuture<Blob> readBlob(String gcsUrl) {
    logger.debug(String.format("Reading GCS file: %s", gcsUrl));
    return CompletableFuture.supplyAsync(
        () -> {
          Blob blob =
              gcsClientProvider
                  .getGcsClient()
                  .get(
                      BlobId.of(
                          storageUtils.getGcsBucketNameFromPath(gcsUrl),
                          storageUtils.getPathFromUrl(gcsUrl)));
          if (blob != null) {
            return blob;
          } else {
            throw new RuntimeException("Blob not found");
          }
        });
  }

  @Override
  public CompletableFuture<InputStream> readFileAsInputStream(String gcsUrl) {
    return readBlob(gcsUrl).thenApply(blob -> Channels.newInputStream(blob.reader()));
  }

  @Override
  public CompletableFuture<byte[]> readFileAsBytes(String gcsUrl) {
    return readBlob(gcsUrl).thenApply(Blob::getContent);
  }
}
