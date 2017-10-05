import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;

public class QuadTree {
    double qullat, qullon, qlrlat, qlrlon, qlondpp, qw, qh;
    int depth;
    double rullat, rullon, rlrlat, rlrlon;
    boolean success;
    private Node root;


    QuadTree(Map<String, Double> params) {
        root = new Node(MapServer.ROOT_ULLAT, MapServer.ROOT_ULLON,
                MapServer.ROOT_LRLAT, MapServer.ROOT_LRLON, "");
        qullat = params.get("ullat");
        qullon = params.get("ullon");
        qlrlat = params.get("lrlat");
        qlrlon = params.get("lrlon");
        qw = params.get("w");
        qh = params.get("h");
        qlondpp = (qlrlon - qullon) / qw;
    }

    public String[][] getGrid() {
        ArrayList<Node> answer = new ArrayList<>();
        gridHelper(root, answer);
        Collections.sort(answer);
        ArrayList<Node> sameLat = new ArrayList<>();
        ArrayList<Node> sameLon = new ArrayList<>();
        Node comp = answer.get(0);
        int width = 0;
        int height = 0;
        for (Node n : answer) {
            if (comp.ullat == n.ullat) {
                width++;
            }
            if (comp.ullon == n.ullon) {
                height++;
            }
        }
        String[][] result = new String[height][width];
        int row = 0;
        int col = 0;
        for (int i = 0; i < answer.size(); i++) {
            result[row][col] = "img/" + answer.get(i).name + ".png";
            col++;
            if (col == width) {
                row++;
                col = 0;
            }
        }
        depth = answer.get(0).name.length();
        rullat = answer.get(0).ullat;
        rullon = answer.get(0).ullon;
        rlrlat = answer.get(answer.size() - 1).lrlat;
        rlrlon = answer.get(answer.size() - 1).lrlon;
        success = true;
        return result;
    }

    public void gridHelper(Node n, ArrayList<Node> answer) {
        if (n.intersectsQuery()) {
            if (n.lonDPPSmaller()) {
                answer.add(n);
            } else {
                double middlelat = (n.ullat - n.lrlat) / 2 + n.lrlat;
                double middlelon = (n.lrlon - n.ullon) / 2 + n.ullon;
                Node one = new Node(n.ullat, n.ullon, middlelat, middlelon, n.name + "1");
                Node two = new Node(n.ullat, middlelon, middlelat, n.lrlon, n.name + "2");
                Node three = new Node(middlelat, n.ullon, n.lrlat, middlelon, n.name + "3");
                Node four = new Node(middlelat, middlelon, n.lrlat, n.lrlon, n.name + "4");
                gridHelper(one, answer);
                gridHelper(two, answer);
                gridHelper(three, answer);
                gridHelper(four, answer);
            }
        }
    }

    public int getDepth() {
        return depth;
    }

    public double getRullat() {
        return rullat;
    }

    public double getRullon() {
        return rullon;
    }

    public double getRlrlat() {
        return rlrlat;
    }

    public double getRlrlon() {
        return rlrlon;
    }

    public boolean getSuccess() {
        return success;
    }

    private class Node implements Comparable<Node> {
        double ullat, ullon, lrlat, lrlon, londpp;
        Node one, two, three, four;
        String name;

        //will only calculate children if found necessary

        Node(double a, double b, double c, double d, String n) {
            ullat = a;
            ullon = b;
            lrlat = c;
            lrlon = d;
            londpp = (lrlon - ullon) / 256;
            name = n;
        }

        public boolean intersectsQuery() {
            if (ullat < qlrlat || qullat < lrlat) {
                return false;
            }
            if (ullon > qlrlon || qullon > lrlon) {
                return false;
            }
            return true;
        }

        public boolean lonDPPSmaller() {
            return ((londpp <= qlondpp) || name.length() == 7);
        }

        @Override
        public int compareTo(Node o) {
            if (ullat == o.ullat) {
                if (ullon == o.ullon) {
                    return 0;
                } else if (ullon > o.ullon) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (ullat < o.ullat) {
                return 1;
            } else {
                return -1;
            }
        }
    }

}

