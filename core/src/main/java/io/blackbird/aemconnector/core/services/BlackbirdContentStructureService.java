package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.models.BlackbirdContentStructureModel;

public interface BlackbirdContentStructureService {
    /**
     * Returns list of nodes for specific path with a depth 1 .
     * Allow whitelisting props, filtering response to keep only listed props
     *
     * @param path the root path to start the traversal (e.g. /content/mywebsite)
     * @return a ContentStructureModel representing the childs of content, or null if not found
     */
    BlackbirdContentStructureModel getContentStructure(String path);
}
