# Hudi-Metadata-Extractor
The Onehouse Community Edition Metadata Extractor is a tool designed for Hudi community users to ship their Hudi commit metrics from their data lake to Onehouse. The tool monitors and continuously discovers tables in the provided cloud file storage path(s) and incrementally pushes the commit stats to Onehouse. Community Edition users can then visualize and analyze their data lake metrics with Onehouse UI.

### Key aspects of its operation include:
- Data Security: The tool interacts exclusively with metadata. It does not access, move, or control actual data files, ensuring the integrity and confidentiality of your data.
- Operational Modes:
  - CONTINUOUS Mode: The tool periodically discovers and uploads metadata for tables found in the configured path. Table discovery happens every 30minutes and new commit instants for the files are discovered and extracted every 5minutes (provided the previous run has completed).
  - ONCE Mode: Allows users to trigger the discovery and extraction flow on demand, the tool picks up from where it left off on the last run. This can be useful if you want to run the metadata extractor as a recurring job that you manage.
- Efficient Data Processing: Utilizes checkpoints for incremental metadata extraction, enabling frequent and efficient metric updates.

## Installation
#### The Onehouse Community Edition Metadata Extractor is available in three formats:
- JAR File: Download the JAR from the assets page in this repo and execute it with the required parameters.
- Docker Image: pull from docker hub and run in supported environments
- Helm Chart: Deploy the tool in your Kubernetes cluster using our Helm chart.

# Configuration
The configuration file for the Onehouse Community Edition Metadata Extractor is a YAML file
that specifies various settings required for the tool's operation.
Below is a detailed explanation of each section and its respective fields within the configuration file:

#### YAML Configuration File Structure
``` YAML
version: V1

onehouseClientConfig:
# can be obtained from the community edition UI
projectId: <CE project id>
apiKey: <api key>
apiSecret: <api secret>
userId: <user id>

fileSystemConfiguration:
# Provide either s3Config or gcsConfig
s3Config:
region: <aws-region>
accessKey: <optional>
accessSecret: <optional>
gcsConfig:
projectId: <optional projectId>
gcpServiceAccountKeyPath: <optional path_to_gcp_auth_key>

metadataExtractorConfig:
jobRunMode: CONTINUOUS | ONCE
pathExclusionPatterns: [<pattern1>, <pattern2>, ...]
parserConfig:
- lake: <lake1>
databases:
- name: <database1>
basePaths: [basepath11, basepath12, ...]
- name: <database2>
basePaths: [<path1>, <path2>, ...]
# Add additional lakes and databases as needed
```

### version
**Description:** Specifies the configuration format version.\
**Format:** String\
**Example:** version: V1\
`Note: Currently, only version V1 is supported`

### onehouseClientConfig
**Description:** Contains credentials for communicating with the Community Edition UI. these values can be obtained from the UI\
**projectId:** Your Community Edition project ID. Get this by clicking on your profile in the top right of the Onehouse UI.\
**userUuid:** The user ID for accessing the service. Get this by clicking on your profile in the top right of the Onehouse UI.\
**apiKey:** The API key for authentication. Get this by opening Settings > API Settings in the Onehouse UI and creating an API key.\
**apiSecret:** The corresponding secret for the API key. Get this by opening Settings > API Settings in the Onehouse UI and creating an API key.\

### fileSystemConfiguration
**Description:** Authentication configuration to access file system, we support either AWS S3 or Google Cloud Storage (GCS).

#### s3Config:
**region:** AWS region of the S3 bucket.\
**[Optional] accessKey:** AWS access key (not recommended for production).\
**[Optional] accessSecret:** AWS secret key (not recommended for production).\
`Note: If access keys are not provided, we use the default AWS credentials chain. For example, you can run the package in an EC2 instance with IAM access to read from S3.`

#### gcsConfig:
**[Optional] projectId:** GCP project ID.
**[Optional] gcpServiceAccountKeyPath:** Path to the GCP service account key.
`Note: If a service account key is not provided, we use the application default credentials. For example, you can run the package in a Compute Engine instance with IAM access to read from GCS.`

### metadataExtractorConfig
**Description:** Configuration for the metadata extraction job.\
**jobRunMode:** Can be CONTINUOUS or ONCE.\
**pathExclusionPatterns:** List of regex patterns to exclude from scanning. (Java regex patterns are supported)\
**parserConfig:** List of lakes and databases to be parsed.\
**lake:** Name of the lake (optional, defaults to community-lake ). This can be used to organize tables in the UI under the format Lake > Database > Table.\
**databases:** List of databases and their respective base paths. This can be used to organize tables in the UI under the format Lake > Database > Table.\
**name:** Database name (optional, defaults to community-db ).\
**basePaths:** List of paths which the extractor needs to look into to find hudi tables. the paths can be paths to hudi tables or a path to a directory containing hudi tables

# Deployment using Jar or Docker-Image
The Onehouse Community Edition Metadata Extractor can be configured using command line arguments for both the JAR file and the Docker image:
#### YAML Configuration String: Use -c followed by the YAML configuration string.
```BASH
java -jar <jar_path> -c '<yaml_string>'

# or for Docker:
docker run <image_name> -c '<yaml_string>'
```
#### Configuration File Path: Use -p followed by the local file system path to the YAML configuration file.
```BASH
java -jar <jar_path> -p '<path_to_config_file>'

# or for Docker:
docker run <image_name> -p '<path_to_config_file>'
```

`[Note: for Docker use -v  to mount the volume where the config file is present when testing  locally]`

# Deployment to kubernetes using helm
1. clone this repo
2. navigate into the helm-chart folder by running\
    `cd helm-chart`
3. create the values.yaml file, refer to the fields in [supported_values](helm-chart/values.yaml)
4. install using helm\
   `helm install hudi-metadata-extractor . -f <path to values.yaml>`
# Known Limitations of the Onehouse Community Edition Metadata Extractor
When using the Onehouse Community Edition Metadata Extractor,
it's important to be aware of certain limitations
that can affect its functionality and the accuracy of the metrics provided.
Understanding these limitations will help in planning and managing your data more effectively.
1. Issues with Re-created Tables in the Same Path\
   **Scenario:** If a Hudi table is deleted and a new table is created in the same path, the tool assigns the same table ID as the previous one.\
   **Implication:** This can lead to inaccuracies in the metrics calculation.\
   **Resolution:** Users should delete such tables from the Onehouse UI to avoid metric calculation issues.
2. Changing Lake or Database Names After Metric Ingestion\
   **Scenario:** If the lake or database name for a given base path is changed after metrics have already been ingested, the tool will not use the new names.\
   **Implication:** Continuity in metric tracking may be lost due to the change in identifiers.\
   **Resolution:** Users should delete the table from the UI before making such changes
3. Pausing and Resuming Metadata Ingestion\
   **Scenario:** If metadata ingestion for a table is paused and then resumed after a few days, there's a risk that the metrics displayed may be inaccurate. (mainly depends on archival configs) \
   **Reason:** The tool processes the archived timeline only once when the table is first discovered. If new instants are committed and moved to the archived timeline while the tool is not running, these instants will not be uploaded upon resumption, leading to potential data loss.\
   **Implication:** There can be a mismatch between the actual table state and the metrics shown.\
   **Resolution:** Users should delete the table from the UI before running the tool again in case of long wait times.

# Data being sent to Onehouse Control-Plane:
- the instant files in the active and archived timeline of the tables
- lastModifiedAt timestamp of some of the instant files, this is used as part of the checkpoint