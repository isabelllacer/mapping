import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses GraphBuildingHandler to convert the XML files into a graph.
 */
public class GraphDB {
    private HashMap<Long, ArrayList<Node>> edges = new HashMap<>();
    private ArrayList<Long> nodeIDs = new ArrayList<>();
    private HashMap<Long, Node> nodes = new HashMap<>();

    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    public int compare(Long o1, Long o2) {
        return nodes.get(o1).compareTo(nodes.get(o2));
    }

    public void set(Long l, Long t, boolean root, Long prev) {
        if (root) {
            nodes.get(l).g = 0;
            nodes.get(l).h = distance(l, t);
            nodes.get(l).t = nodes.get(t);
            nodes.get(l).prev = null;
            return;
        }
        nodes.get(l).h = distance(l, t);
        nodes.get(l).t = nodes.get(t);
        nodes.get(l).prev = nodes.get(prev);
        //g is parent's g plus this g, the dist between prev and this node
        nodes.get(l).g = nodes.get(prev).g + distance(l, prev);
        nodes.get(l).priority = nodes.get(l).g + nodes.get(l).h;
    }

    public double getPriority(Long l) {
        Node curr = nodes.get(l);
        if (curr.priority == -1) {
            throw new RuntimeException("ERROR: Priority not yet set");
        }
        return curr.priority;
    }

    public double getG(Long l) {
        return nodes.get(l).g;
    }

    public long prev(Long id) {
        if (nodes.get(id).prev != null) {
            return nodes.get(id).prev.id;
        }
        return 0;
    }

    public void addNode(double nlat, double nlon, Long id) {
        new Node(nlat, nlon, id);
    }

    public void addName(Long id, String name) {
        nodes.get(id).name = name;
    }

    public void addWay(ArrayList<Long> nds) {
        long prev = nds.get(0);
        long next = nds.get(1);
        for (int i = 1; i < nds.size(); i++) {
            addEdge(prev, next);
            prev = next;
            if (i < nds.size() - 1) {
                next = nds.get(i + 1);
            }
        }
    }

    public void addEdge(Long a, Long b) {
        edges.get(a).add(nodes.get(b));
        edges.get(b).add(nodes.get(a));
    }

    /**
     * Remove edges with no connections from the graph.
     * While this does not guarantee that any two edges in the remaining graph are connected,
     * can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        /*
        ArrayList<Long> toRem = new ArrayList<>();
        for (Long l : edges.keySet()) {
            if (edges.get(l).size() == 0) {
                nodes.remove(l);
                nodeIDs.remove(l);
                toRem.add(l);
            }
        }
        for (Long l : toRem) {
            edges.remove(l);
        }
        */
        Iterator<Long> it = nodeIDs.iterator();
        while (it.hasNext()) {
            Long l = it.next();
            if (edges.get(l).size() == 0) {
                edges.remove(l);
                nodes.remove(l);
                it.remove();
            }
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        return nodeIDs;
    }

    /**
     * Returns ids of all vertices adjacent to v.
     */
    Iterable<Long> adjacent(long v) {
        ArrayList<Long> answer = new ArrayList<>();
        for (Node n : edges.get(v)) {
            answer.add(n.id);
        }
        return answer;
    }

    /**
     * Returns the Euclidean distance between vertices v and w, where Euclidean distance
     * is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ).
     */
    double distance(long v, long w) {
        Node one = nodes.get(v);
        Node two = nodes.get(w);
        return Math.sqrt((one.lon - two.lon) * (one.lon - two.lon)
                + (one.lat - two.lat) * (one.lat - two.lat));
    }

    /**
     * Returns the vertex id closest to the given longitude and latitude.
     */
    long closest(double lon, double lat) {
        double min = -1;
        Node n = null;
        for (Long l : nodeIDs) {
            Node curr = nodes.get(l);
            double currDist = eucDist(lon, lat, nodes.get(l));
            if (min == -1 || currDist < min) {
                n = curr;
                min = currDist;
            }
        }

        return n.id;

    }

    public Node getNode(Long l) {
        return nodes.get(l);
    }


    double eucDist(double lon, double lat, Node n) {
        return Math.sqrt((lon - n.lon) * (lon - n.lon) + (lat - n.lat) * (lat - n.lat));
    }

    /**
     * Longitude of vertex v.
     */
    double lon(long v) {
        return nodes.get(v).lon;
    }

    /**
     * Latitude of vertex v.
     */
    double lat(long v) {
        return nodes.get(v).lat;
    }

    public void reset(Long l) {
        nodes.get(l).reset();
    }

    public void resetPrev(Long l) {
        nodes.get(l).resetPrevious();
    }

    class Node implements Comparable<Node> {
        double lat, lon;
        Long id;
        String name;
        //Needed for shortestRoute
        Node prev = null;
        Node t = null;
        double g, h, priority = -1;

        Node(double nlat, double nlon, Long n) {
            lat = nlat;
            lon = nlon;
            id = n;
            edges.put(id, new ArrayList<Node>());
            nodes.put(id, this);
            nodeIDs.add(id);
            priority = -1;
        }

        @Override
        public int compareTo(Node other) {
            if (getPriority(this.id) < getPriority(other.id)) {
                return -1;
            }
            if (getPriority(this.id) > getPriority(other.id)) {
                return 1;
            }
            return 0;
        }

        void reset() {
            priority = -1;
            t = null;
            g = 0;
            h = 0;
        }

        void resetPrevious() {
            prev = null;
        }

    }
}
