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
     * Cache that's used to store the result of {@link #getExtension(java.lang.String)}
     */
    static final Map<String, String> EXTENSION_CACHE = new HashMap<>()

    static final String DOWNLOAD_URL = "https://cdn.modrinth.com/data/"

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
        if(attributes.get("organisation") == "modrinth.maven") {
            try {
                Optional<String> result = getExtension(attributes.get("module"), attributes.get("revision"))
                if(result.isPresent()) {
                    return result.get()
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
     * @param versionId the project slug if {@code group} is `modrinth.maven`, or the project id if it's `modrinth.maven.id`
     * @return the extension for the given artifacts.
     */
    static Optional<String> getExtension(String modId, String versionId) {
        println("Extension: $modId $versionId")

        //Gets the cache key for this object. the classifier can be null, hence why Objects.toString is used.
        def cacheKey = "$modId:$versionId"

        //If the cache exists, return it
        def cache = EXTENSION_CACHE.get(cacheKey)
        if(cache != null) {
            return Optional.of(cache)
        }

        def modJson = new URL("https://api.modrinth.com/api/v1/mod/").content
        println(modJson)
        if (modJson.isEmpty()) {
            throw new IllegalArgumentException("Mod ID is invalid. ModId: $modId")
        }
        JsonObject modJsonObject = GSON.fromJson(modJson, JsonObject.class)
        JsonArray fileList = modJsonObject.getAsJsonArray("versions")
        boolean contains = false;
        fileList.forEach({ jsonElement ->
            if (versionId == jsonElement.asString) {
                contains = true
            }
        })

        if (!contains) {
            throw new IllegalArgumentException("Can't find Version ID in Version List of Mod ID. VersionId: $versionId, ModId: $modId")
        }

        def fileJson = new URL("https://api.modrinth.com/api/v1/version/$versionId").content
        println(fileJson)
        JsonObject fileJsonObject = GSON.fromJson(fileJson, JsonObject.class)
        JsonObject fileInfo = fileJsonObject.getAsJsonArray("files").get(0) as JsonObject
        //Get the normal jar result. This should never be empty.
        def url = fileInfo.get("url").getAsString()
        println(url)
        def result = new URL(url).text
        if(result.isEmpty()) {
            throw new IllegalArgumentException("Version ID is invalid. VersionId: '$versionId'")
        }

//        //If we need to search for a classifier, then do so
//        if(classifier != null) {
//            //Get the normal, no classifier jar name, and the file id for that jar name
//            def jarName = result.substring(result.lastIndexOf('/'), result.length() - ".jar".length())
//            def start = Integer.parseInt(fileID)
//            boolean found = false
//
//            //Go from the current file version to 20 + the current file version.
//            //For each version, get the download url and see if it matches with the found jar name, along with the -classifier prefix.
//            //If so then set the result to that and mark the classifier as found
//            for(int i = 1; i <= 20; i++) {
//                try {
//                    def tryResult = new URL("https://addons-ecs.forgesvc.net/api/v2/addon/0/file/${start + i}/download-url").text
//                    if(tryResult.endsWith("$jarName-${classifier}.jar")) {
//                        result = tryResult
//                        found = true
//                        break
//                    }
//                } catch(FileNotFoundException ignored) {
//                    break
//                }
//            }
//
//            //Classifier could not be found, this is fine.
//            if(!found) {
//                return Optional.empty()
//            }
//        }

        EXTENSION_CACHE.put(cacheKey, result)
        Optional.ofNullable(result)
    }
}

