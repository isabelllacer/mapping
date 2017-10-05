import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

/**
 * Parses OSM XML files using an XML SAX parser. Used to construct the graph of roads for
 * pathfinding, under some constraints.
 */
public class GraphBuildingHandler extends DefaultHandler {
    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));
    private final GraphDB g;
    private String activeState = "";
    private ArrayList<Long> curr_way = new ArrayList<>();
    private boolean curr_valid = false;
    private Long curr_node;

    public GraphBuildingHandler(GraphDB g) {
        this.g = g;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equals("node")) {
            activeState = "node";
            g.addNode(Double.parseDouble(attributes.getValue("lat")),
                    Double.parseDouble(attributes.getValue("lon")), Long.parseLong(attributes.getValue("id")));
            curr_node = Long.parseLong(attributes.getValue("id"));

        } else if (qName.equals("way")) {
            /* We encountered a new <way...> tag. */
            activeState = "way";
        } else if (activeState.equals("way") && qName.equals("nd")) {
            /* While looking at a way, we found a <nd...> tag. */
            curr_way.add(Long.parseLong(attributes.getValue("ref")));

        } else if (activeState.equals("way") && qName.equals("tag")) {
            /* While looking at a way, we found a <tag...> tag. */
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("maxspeed")) {
            } else if (k.equals("highway")) {
                if (ALLOWED_HIGHWAY_TYPES.contains(v)) {
                    curr_valid = true;
                }
            } else if (k.equals("name")) {
            }
        } else if (activeState.equals("node") && qName.equals("tag") && attributes.getValue("k")
                .equals("name")) {
            /* While looking at a node, we found a <tag...> with k="name". */
            g.addName(curr_node, attributes.getValue("v"));
        }
    }

    /**
     * Receive notification of the end of an element.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            /* We are done looking at a way.*/
            if (curr_valid && curr_way.size() > 1) {
                g.addWay(curr_way);
            }
            curr_valid = false;
            curr_way.clear();
        }
    }

}
