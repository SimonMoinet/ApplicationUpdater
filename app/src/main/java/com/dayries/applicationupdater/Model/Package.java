package com.dayries.applicationupdater.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Package {

    @SerializedName("version")
    @Expose
    private String version;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("releaseNotes")
    @Expose
    private List<String> releaseNotes = null;

    public Version getVersion() {
        return new Version(version);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(List<String> releaseNotes) {
        this.releaseNotes = releaseNotes;
    }
}
