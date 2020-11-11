package io.github.vampirestudios.modrinthmaven


import org.gradle.api.internal.artifacts.repositories.resolver.M2ResourcePattern
import org.gradle.api.internal.artifacts.repositories.resolver.MavenPattern

import com.google.gson.*

/**
 * The pattern that's used to take in the file id of the modrinth file,
 * and delegate it to the actual modrinth maven repository.
 * @author Wyn Price
 */
class ModrinthResourcePattern extends M2ResourcePattern {

    /**
     * Cache that's used to store the result of {@link #getExtension(java.lang.String, java.lang.String)}
     */
    static final Map<String, String> EXTENSION_CACHE = new HashMap<>()

    static final String DOWNLOAD_URL = "https://cdn.modrinth.com"

    static final Gson GSON = new GsonBuilder().create()

    ModrinthResourcePattern() {
        super(new URI(DOWNLOAD_URL), MavenPattern.M2_PATTERN)
    }

    @Override
    String getPattern() {
        return "Modrinth Delegate Pattern"
    }

    @Override
    protected String substituteTokens(String pattern, Map<String, String> attributes) {
        //If the organization is equal to `modrinth.`maven, then try and resolve it.
        def modIdMatcher = attributes.get("module") =~ /^[a-zA-Z0-9]+/
        def versionIdMatcher = attributes.get("revision") =~ /^[a-zA-Z0-9]+/
        if(attributes.get("organisation") == "modrinth.maven" && versionIdMatcher.find() && modIdMatcher.find()) {
            try {
                Optional<String> result = getExtension(modIdMatcher.group(0), versionIdMatcher.group(0))
                if(result.isPresent()) {
                    return result.get().replace(DOWNLOAD_URL, "")
                }
            } catch(Exception e) {
                println e.message
                e.printStackTrace()
            }
        }
        return super.substituteTokens(pattern, attributes)
    }

    /**
     * Gets the suffix for {@link #DOWNLOAD_URL}. Used to resolve the URL maven patterns. NOTE: ANY EXCEPTION THROWN BY THIS WILL JUST BE CONSUMED
     * @param modId the project id
     * @param versionId the version id
     * @return the extension for the given artifacts.
     */
    static Optional<String> getExtension(String modId, String versionId) {

        //Gets the cache key for this object. the classifier can be null, hence why Objects.toString is used.
        def cacheKey = "$modId:$versionId"

        //If the cache exists, return it
        def cache = EXTENSION_CACHE.get(cacheKey)
        if(cache != null) {
            return Optional.of(cache)
        }

        def modJson = new InputStreamReader(new URL("https://api.modrinth.com/api/v1/mod/$modId").openStream())
        
        JsonObject modJsonObject = GSON.fromJson(modJson, JsonObject.class)
        JsonArray fileList = modJsonObject.getAsJsonArray("versions")
        boolean contains = false;
        fileList.forEach({ jsonElement ->
            if (versionId == jsonElement.asString) {
                contains = true
            }
        })

        if (!contains && !versionId.contains("@")) {
            throw new IllegalArgumentException("Can't find Version ID in Version List of Mod ID. VersionId: $versionId, ModId: $modId")
        }

        def fileJson = new InputStreamReader(new URL("https://api.modrinth.com/api/v1/version/$versionId").openStream())
        JsonObject fileJsonObject = GSON.fromJson(fileJson, JsonObject.class)
        JsonObject fileInfo = fileJsonObject.getAsJsonArray("files").get(0) as JsonObject

        def result = fileInfo.get("url").getAsString()

        EXTENSION_CACHE.put(cacheKey, result)
        Optional.ofNullable(result)
    }
}

