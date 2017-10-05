import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result.
 */
public class Rasterer {
    public Rasterer(String imgRoot) {
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end.
     */

    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();
        QuadTree qt = new QuadTree(params);
        results.put("render_grid", qt.getGrid());
        results.put("depth", qt.getDepth());
        results.put("raster_ul_lon", qt.getRullon());
        results.put("raster_ul_lat", qt.getRullat());
        results.put("raster_lr_lon", qt.getRlrlon());
        results.put("raster_lr_lat", qt.getRlrlat());
        results.put("query_success", qt.getSuccess());
        return results;
    }


}
