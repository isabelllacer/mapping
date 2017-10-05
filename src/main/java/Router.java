import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.ArrayList;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map.
 */
public class Router {
    /**
     * Returns a LinkedList of <code>Long</code>s representing the shortest path from st to dest,
     * where the longs are node IDs.
     */
    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                                double destlon, double destlat) {

        GraphDB.Node s = g.getNode(g.closest(stlon, stlat));
        GraphDB.Node t = g.getNode(g.closest(destlon, destlat));
        PriorityQueue<GraphDB.Node> pq = new PriorityQueue<>(20);
        ArrayList<GraphDB.Node> seen = new ArrayList<>();
        double soFar = 0;


        GraphDB.Node curr = s;
        g.set(s.id, t.id, true, null);

        pq.add(s);
        seen.add(curr);


        while (!curr.equals(t)) {
            curr = pq.poll();
            if (curr.id != t.id) {

                for (long l : g.adjacent(curr.id)) {

                    GraphDB.Node n = g.getNode(l);
                    soFar = g.getG(curr.id) + g.distance(curr.id, n.id);

                    if (!seen.contains(n)) {
                        g.set(n.id, t.id, false, curr.id);
                        pq.add(n);
                        seen.add(n);
                    } else if (n.id != g.prev(curr.id)) {
                        if (g.getPriority(l) > soFar + g.distance(l, t.id)) {
                            if (pq.contains(n)) {
                                pq.remove(n);
                            }

                            g.set(n.id, t.id, false, curr.id);
                            pq.add(n);
                        }
                    }
                }
            }
        }

        LinkedList<Long> answer = new LinkedList<>();
        while (curr != null) {
            GraphDB.Node old = curr;
            answer.add(0, curr.id);
            curr = g.getNode(g.prev(curr.id));
            g.reset(old.id);
            g.resetPrev(old.id);
        }

        return answer;
    }

}
