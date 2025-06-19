# Blackbird AEM Connector

## Blackbird AEM Connector –  OpenAPI 3.0
The YAML specification [included in the documentation](https://github.com/bb-io/AEM/blob/main/docs/openapi/BlackBird%20AEM%20connector.yaml) follows the OpenApi 3.0 standard and is designed to be compatible with Swagger UI and other OpenAPI tools.

You can paste this YAML directly into tools like [Swagger Editor](https://editor-next.swagger.io/) or integrate it into an internal API portal for interactive exploration

## Prerequisites
Even though this repository is **public**, GitHub **requires authentication** to download packages from **GitHub Packages**, including for **Maven dependencies**. This is a security feature to prevent anonymous abuse of GitHub's infrastructure.

For the on-premise version, follow the steps from [the instruction](https://github.com/bb-io/AEM/blob/main-aem-on-prem/README.md) to embed BB AEM Connector to a project or download [the latest version](https://github.com/bb-io/AEM/packages/2548678) and install it through Package Manager manually.

For AEMaaCS version, follow the next steps to generate a token and configure Maven to use it:

### Step 1: Create a GitHub Personal Access Token (PAT)

#### 1.1 Open the Token Generator

[Generate a new token (classic) with the `read:packages` scope](https://github.com/settings/tokens/new?scopes=read:packages)

#### 1.2 Configure the Token

- **Note**: `Maven Access Token`
- **Expiration**: Choose `30 days`, `90 days`, or `No expiration`
- **Scope**:
    - `read:packages` (only this is needed)

> This token will **only allow read access to published packages**, and cannot modify repositories or access private code.

#### 1.3 Click "Generate Token"

Copy the token **immediately** — you won’t be able to see it again.

### Step 2: Configure Maven to Use the Token

#### 2.1 Add to settings.xml
Create a file in your repository called `settings.xml` (or modify the current one) in your AEM as a Cloud Service git repository. The path for this file should be `.cloudmanager/maven/settings.xml`:
Add the following block:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <servers>
    <server>
      <id>bb-io-AEM-github</id>
      <username>GITHUB_USERNAME</username>
      <password>PASTE_YOUR_PAT_HERE</password>
    </server>
  </servers>
</settings>
```
#### 2.2 Replace:
 - `GITHUB_USERNAME` with your GitHub username
 - `PASTE_YOUR_PAT_HERE` with the token you just generated

### Step 3: Add the Repository in your root pom.xml
```xml
<repositories>
    <repository>
        <id>bb-io-AEM-github</id>
        <url>https://maven.pkg.github.com/bb-io/AEM</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```
### Step 4: Add the Dependency in your root pom.xml
#### 4.1 Update pom.xml with
```xml
<dependency>
  <groupId>io.blackbird</groupId>
  <artifactId>bb-aem-connector.all</artifactId>
  <type>zip</type>
  <version>{current_version}</version>
</dependency>
```
#### 4.2 Replace placeholder {current_version} with the latest version.

### Step 5: Add the Dependency in your /all/pom.xml
```xml
<dependency>
    <groupId>io.blackbird</groupId>
    <artifactId>bb-aem-connector.all</artifactId>
    <type>zip</type>
</dependency>
```
### Step 6: Add the package as an embedded dependency to your /all/pom.xml
```xml
<embedded>
  <groupId>io.blackbird</groupId>
  <artifactId>bb-aem-connector.all</artifactId>
  <type>zip</type>
  <target>/apps/bb-vendor-packages/application/install</target>
</embedded>
```
> NOTE: Add path `/apps/bb-vendor-packages/application/install` to `filter.xml` for `all` module or change target path for the embed.

### Step 7: Commit all the changes to the branch and push the changes

### Step 8: The build for AEM as a Cloud Service can now be done

## Configure Authorization (JWT)
Follow the [official documentation](https://experienceleague.adobe.com/en/docs/experience-manager-learn/getting-started-with-aem-headless/authentication/service-credentials) to create a Technical Account for the needed AEM Author program or use the next steps.

### Steps to create Technical Account
1. Open [Cloud Manager](https://experience.adobe.com/cloud-manager/landing.html).
2. Select needed program. ![image auth step 2](docs/images/auth_step_2.png)
3. Open Developer Console for needed Author environment. ![image auth step 3](docs/images/auth_step_3.png)
4. Switch to `Integrations` tab and `Create new technical account`. ![image auth step 4](docs/images/auth_step_4.png)
5. Unfold created private key and `View` the data. ![image auth step 5](docs/images/auth_step_5.png)
6. Use the `Download` button to obtain the raw data and store it in a file or another location from which it will be used for integration. ![image auth step 6](docs/images/auth_step_6.png)

### Validate integration
To validate the integration, code samples for different programming languages can be used ([GitHub link](https://github.com/AdobeDocs/adobe-dev-console/blob/main/samples/adobe-jwt-dotnet/Program.cs)).
