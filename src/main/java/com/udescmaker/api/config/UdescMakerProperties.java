package com.udescmaker.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("udescmaker")
public class UdescMakerProperties {
    private URI apiPublicUrl = URI.create("http://localhost:8080");
    private URI siteUrl = URI.create("https://samuelgislon.github.io/udescmaker");
    private final Catalog catalog = new Catalog();
    private final Github github = new Github();
    private final Uploads uploads = new Uploads();
    private final Cors cors = new Cors();

    public URI getApiPublicUrl() { return apiPublicUrl; }
    public void setApiPublicUrl(URI apiPublicUrl) { this.apiPublicUrl = apiPublicUrl; }
    public URI getSiteUrl() { return siteUrl; }
    public void setSiteUrl(URI siteUrl) { this.siteUrl = siteUrl; }
    public Catalog getCatalog() { return catalog; }
    public Github getGithub() { return github; }
    public Uploads getUploads() { return uploads; }
    public Cors getCors() { return cors; }

    public static class Catalog {
        private String mode = "local";
        private Path localPath = Path.of("../udescmaker/src/content/projects");
        private Duration cacheTtl = Duration.ofMinutes(5);
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public Path getLocalPath() { return localPath; }
        public void setLocalPath(Path localPath) { this.localPath = localPath; }
        public Duration getCacheTtl() { return cacheTtl; }
        public void setCacheTtl(Duration cacheTtl) { this.cacheTtl = cacheTtl; }
    }

    public static class Github {
        private String owner = "SamuelGislon";
        private String repository = "udescmaker";
        private String branch = "main";
        private String projectsPath = "src/content/projects";
        private String token = "";
        private boolean publishEnabled;
        private URI apiUrl = URI.create("https://api.github.com");
        private URI rawUrl = URI.create("https://raw.githubusercontent.com");
        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
        public String getRepository() { return repository; }
        public void setRepository(String repository) { this.repository = repository; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        public String getProjectsPath() { return projectsPath; }
        public void setProjectsPath(String projectsPath) { this.projectsPath = projectsPath; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public boolean isPublishEnabled() { return publishEnabled; }
        public void setPublishEnabled(boolean publishEnabled) { this.publishEnabled = publishEnabled; }
        public URI getApiUrl() { return apiUrl; }
        public void setApiUrl(URI apiUrl) { this.apiUrl = apiUrl; }
        public URI getRawUrl() { return rawUrl; }
        public void setRawUrl(URI rawUrl) { this.rawUrl = rawUrl; }
    }

    public static class Uploads {
        private long maxFileBytes = 20L * 1024 * 1024;
        private long maxTotalBytes = 100L * 1024 * 1024;
        public long getMaxFileBytes() { return maxFileBytes; }
        public void setMaxFileBytes(long maxFileBytes) { this.maxFileBytes = maxFileBytes; }
        public long getMaxTotalBytes() { return maxTotalBytes; }
        public void setMaxTotalBytes(long maxTotalBytes) { this.maxTotalBytes = maxTotalBytes; }
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:8081", "http://localhost:19006"));
        private boolean allowCredentials;
        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        public boolean isAllowCredentials() { return allowCredentials; }
        public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
    }
}
